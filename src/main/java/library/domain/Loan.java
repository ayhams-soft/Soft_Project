package library.domain;

import library.domain.media.Media;

import java.time.LocalDate;

/**
 * Loan entity.
 */
public class Loan {
    private static long SEQ = 1;
    private final String id;
    private final String userId;
    private final String mediaId;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnedDate;

    public Loan(String userId, String mediaId, LocalDate borrowDate, LocalDate dueDate) {
        this.id = "L" + (SEQ++);
        this.userId = userId;
        this.mediaId = mediaId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getMediaId() { return mediaId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnedDate() { return returnedDate; }
    public void setReturnedDate(LocalDate d) { this.returnedDate = d; }

    public boolean isReturned() { return returnedDate != null; }

    public boolean isOverdue(LocalDate currentDate) {
        if (isReturned()) return false;
        return currentDate.isAfter(dueDate);
    }

    public int overdueDays(LocalDate currentDate) {
        if (!isOverdue(currentDate)) return 0;
        return (int) (currentDate.toEpochDay() - dueDate.toEpochDay());
    }
    @Override
    public String toString() {
        return "Loan{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", mediaId='" + mediaId + '\'' +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnedDate=" + returnedDate +
                '}';
    }

}
