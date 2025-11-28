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
 * ReminderService collects overdue loans and notifies registered notifiers.
 *
 * Updated behavior:
 * - sendReminders(...) will send a single message per user that includes both:
 *      number of overdue BOOKs and number of overdue CDs.
 * - Message body format:
 *      "You have X overdue book(s) and Y overdue CD(s)."
 *
 * Notifier is a simple functional interface so you can register:
 *  - a fake notifier for testing (records messages),
 *  - or an adapter that delegates to an EmailClient to actually send emails.
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

    public ReminderService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Register a Notifier (Observer). Examples:
     *  - registerNotifier((user, msg) -> emailClient.send(user.getEmail(), "Library reminder", msg));
     *  - registerNotifier((user, msg) -> fakeRecorder.record(user.getEmail(), msg));
     */
    public void registerNotifier(Notifier notifier) {
        if (notifier != null) {
            notifiers.add(notifier);
        }
    }

    /**
     * Send reminders to users that have overdue BOOK or CD loans.
     *
     * New behavior: for each user we compute:
     *  - number of overdue BOOKs
     *  - number of overdue CDs
     *
     * Then we send a single message:
     *   "You have X overdue book(s) and Y overdue CD(s)."
     *
     * @param loanRepo  repository to query loans
     * @param userRepo  repository to query users
     * @param mediaRepo repository to query media (used to check media type)
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
                    System.err.println("ReminderService: notifier failed for user " + userId + " : " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Build an OverdueReport mapping user -> counts and fines using strategies.
     * Kept as before (mixed media supported): counts = number of overdue items (books+cds),
     * sums = computed fines per media type using provided strategies.
     */
    public OverdueReport buildReport(LoanRepository loanRepo, UserRepository userRepo, MediaRepository mediaRepo, library.strategy.FineStrategy bookFine, library.strategy.FineStrategy cdFine) {
        LocalDate today = timeProvider.today();
        List<Loan> overdue = loanRepo.findAll().stream().filter(l -> l.isOverdue(today)).collect(Collectors.toList());
        Map<String, Integer> counts = new HashMap<>();
        Map<String, Integer> sums = new HashMap<>();
        for (Loan l : overdue) {
            String uid = l.getUserId();
            counts.put(uid, counts.getOrDefault(uid, 0) + 1);
            Media m = mediaRepo.findById(l.getMediaId()).orElse(null);
            int overdueDays = l.overdueDays(today);
            int fine = 0;
            if (m != null) {
                if ("BOOK".equals(m.getMediaType())) fine = bookFine.calculateFine(overdueDays);
                else if ("CD".equals(m.getMediaType())) fine = cdFine.calculateFine(overdueDays);
            }
            sums.put(uid, sums.getOrDefault(uid, 0) + fine);
        }
        return new OverdueReport(counts, sums);
    }
}
