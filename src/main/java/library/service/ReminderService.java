package library.service;

import library.dto.EmailMessage;
import library.dto.OverdueReport;
import library.domain.Loan;
import library.domain.User;
import library.domain.media.Media;
import library.repository.LoanRepository;
import library.repository.MediaRepository;
import library.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReminderService collects overdue loans and notifies notifiers.
 * Notifiers are simple function-like interfaces implemented as Observer.
 */
public class ReminderService {

    public interface Notifier {
        void notify(User user, String message);
    }

    private final List<Notifier> notifiers = new ArrayList<>();
    private final TimeProvider timeProvider;

    public ReminderService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public void registerNotifier(Notifier notifier) {
        notifiers.add(notifier);
    }

    /**
     * Send reminders to users that have overdue loans.
     * loanRepo is provided at runtime to query loans (allows test injection).
     */
    public void sendReminders(LoanRepository loanRepo, UserRepository userRepo, MediaRepository mediaRepo) {
        LocalDate today = timeProvider.today();
        List<Loan> overdue = loanRepo.findAll().stream().filter(l -> l.isOverdue(today)).collect(Collectors.toList());
        Map<String, List<Loan>> byUser = overdue.stream().collect(Collectors.groupingBy(Loan::getUserId));
        for (Map.Entry<String, List<Loan>> e : byUser.entrySet()) {
            String userId = e.getKey();
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) continue;
            int count = e.getValue().size();
            String message = "You have " + count + " overdue book(s).";
            for (Notifier n : notifiers) {
                n.notify(user, message);
            }
        }
    }

    /**
     * Build an OverdueReport mapping user -> counts and fines using strategies.
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
