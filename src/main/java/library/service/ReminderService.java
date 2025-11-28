package library.service;

import library.domain.Loan;
import library.domain.User;
import library.domain.media.Media;
import library.dto.OverdueReport;
import library.repository.LoanRepository;
import library.repository.MediaRepository;
import library.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for building overdue reports and sending reminders.
 *
 * Behavior:
 * <ul>
 *     <li>Collects overdue loans based on the current date.</li>
 *     <li>Aggregates them per user.</li>
 *     <li>Notifies all registered notifiers with a simple message.</li>
 * </ul>
 *
 * For reminders:
 * <ul>
 *     <li>Each user gets a single message.</li>
 *     <li>Message format: {@code "You have X overdue book(s) and Y overdue CD(s)."}</li>
 * </ul>
 */
public class ReminderService {

    /**
     * Notifier: functional interface used to perform notification for a user.
     * Implementations decide how to deliver (email, log, etc).
     */
    public interface Notifier {
        void notify(User user, String message);
    }

    private final List<Notifier> notifiers = new ArrayList<>();
    private final TimeProvider timeProvider;

    /**
     * Creates a new ReminderService.
     *
     * @param timeProvider provider used to get the current date
     */
    public ReminderService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Registers a new notifier (observer).
     * <p>Examples:</p>
     * <ul>
     *     <li>{@code registerNotifier((user, msg) -> emailClient.send(user.getEmail(), "Library reminder", msg));}</li>
     *     <li>{@code registerNotifier((user, msg) -> fakeRecorder.record(user.getEmail(), msg));}</li>
     * </ul>
     *
     * @param notifier the notifier implementation to add
     */
    public void registerNotifier(Notifier notifier) {
        if (notifier != null) {
            notifiers.add(notifier);
        }
    }

    /**
     * Sends reminders to users that have overdue BOOK or CD loans.
     * <p>
     * For each user, it computes:
     * <ul>
     *     <li>number of overdue BOOKs</li>
     *     <li>number of overdue CDs</li>
     * </ul>
     * Then sends a single message like:
     * <pre>
     * "You have X overdue book(s) and Y overdue CD(s)."
     * </pre>
     *
     * @param loanRepo  repository used to query loans
     * @param userRepo  repository used to query users
     * @param mediaRepo repository used to query media (and check media type)
     */
    public void sendReminders(LoanRepository loanRepo, UserRepository userRepo, MediaRepository mediaRepo) {
        LocalDate today = timeProvider.today();

        // 1) Find all overdue loans (active + overdue)
        List<Loan> overdueAll = loanRepo.findAll().stream()
                .filter(l -> !l.isReturned() && l.isOverdue(today))
                .collect(Collectors.toList());

        if (overdueAll.isEmpty()) {
            return; // nothing to do
        }

        // 2) For each loan determine its media type (BOOK or CD) and group by user
        // We'll build maps: userId -> bookCount, userId -> cdCount
        Map<String, Integer> bookCounts = new HashMap<>();
        Map<String, Integer> cdCounts = new HashMap<>();

        for (Loan l : overdueAll) {
            Optional<Media> maybeMedia = mediaRepo.findById(l.getMediaId());
            String userId = l.getUserId();
            if (!maybeMedia.isPresent()) continue;

            Media media = maybeMedia.get();
            String type = media.getMediaType();

            if ("BOOK".equals(type)) {
                bookCounts.put(userId, bookCounts.getOrDefault(userId, 0) + 1);
            } else if ("CD".equals(type)) {
                cdCounts.put(userId, cdCounts.getOrDefault(userId, 0) + 1);
            } else {
                // ignore unknown types for reminders
            }
        }

        // 3) Collect union of userIds that have either books or CDs overdue
        Set<String> userIds = new HashSet<>();
        userIds.addAll(bookCounts.keySet());
        userIds.addAll(cdCounts.keySet());

        // 4) For each user send combined message
        for (String userId : userIds) {
            Optional<User> maybeUser = userRepo.findById(userId);
            if (!maybeUser.isPresent()) continue;

            User user = maybeUser.get();
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) continue;

            int books = bookCounts.getOrDefault(userId, 0);
            int cds = cdCounts.getOrDefault(userId, 0);

            String message = "You have " + books + " overdue book(s) and " + cds + " overdue CD(s).";

            for (Notifier notifier : notifiers) {
                try {
                    notifier.notify(user, message);
                } catch (Exception ex) {
                    // Log to stderr to avoid interrupting other notifications
                    System.err.println("ReminderService: notifier failed for user "
                            + userId + " : " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Builds an {@link OverdueReport} that maps each user to:
     * <ul>
     *     <li>number of overdue items (books + CDs)</li>
     *     <li>total fine based on media type and fine strategies</li>
     * </ul>
     *
     * @param loanRepo  repository to read loans from
     * @param userRepo  repository to read users from (kept for future use/extensions)
     * @param mediaRepo repository to read media and detect types
     * @param bookFine  fine strategy used for books
     * @param cdFine    fine strategy used for CDs
     * @return an OverdueReport containing counts and fine totals per user
     */
    public OverdueReport buildReport(LoanRepository loanRepo,
                                     UserRepository userRepo,
                                     MediaRepository mediaRepo,
                                     library.strategy.FineStrategy bookFine,
                                     library.strategy.FineStrategy cdFine) {

        LocalDate today = timeProvider.today();
        List<Loan> overdue = loanRepo.findAll().stream()
                .filter(l -> l.isOverdue(today))
                .collect(Collectors.toList());

        Map<String, Integer> counts = new HashMap<>();
        Map<String, Integer> sums = new HashMap<>();

        for (Loan l : overdue) {
            String uid = l.getUserId();

            // Count of overdue items
            counts.put(uid, counts.getOrDefault(uid, 0) + 1);

            Media m = mediaRepo.findById(l.getMediaId()).orElse(null);
            int overdueDays = l.overdueDays(today);
            int fine = 0;

            if (m != null) {
                if ("BOOK".equals(m.getMediaType())) {
                    fine = bookFine.calculateFine(overdueDays);
                } else if ("CD".equals(m.getMediaType())) {
                    fine = cdFine.calculateFine(overdueDays);
                }
            }

            sums.put(uid, sums.getOrDefault(uid, 0) + fine);
        }

        return new OverdueReport(counts, sums);
    }
}
