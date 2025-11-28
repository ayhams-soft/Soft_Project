package library.domain;

import library.domain.media.Media;
import java.time.LocalDate;

/**
 * Represents a loan operation in the library.
 * A loan stores the userId, mediaId, borrow date, due date, and return date.
 */
public class Loan {

    private static long SEQ = 1;
    private final String id;
    private final String userId;
    private final String mediaId;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnedDate;

    /**
     * Creates a new loan.
     *
     * @param userId     the id of the user who borrowed the item
     * @param mediaId    the id of the media item (book or CD)
     * @param borrowDate the date the item was borrowed
     * @param dueDate    the date the item should be returned
     */
    public Loan(String userId, String mediaId, LocalDate borrowDate, LocalDate dueDate) {
        this.id = "L" + (SEQ++);
        this.userId = userId;
        this.mediaId = mediaId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    /** @return loan id */
    public String getId() { return id; }

    /** @return user id */
    public String getUserId() { return userId; }

    /** @return media id */
    public String getMediaId() { return mediaId; }

    /** @return the borrow date */
    public LocalDate getBorrowDate() { return borrowDate; }

    /** @return the due date */
    public LocalDate getDueDate() { return dueDate; }

    /** @return the return date, or null if not returned yet */
    public LocalDate getReturnedDate() { return returnedDate; }

    /** Sets the return date. */
    public void setReturnedDate(LocalDate d) { this.returnedDate = d; }

    /**
     * Checks if the item has been returned.
     *
     * @return true if returned
     */
    public boolean isReturned() { return returnedDate != null; }

    /**
     * Checks if the loan is overdue.
     *
     * @param currentDate the current system date
     * @return true if the item is overdue
     */
    public boolean isOverdue(LocalDate currentDate) {
        if (isReturned()) return false;
        return currentDate.isAfter(dueDate);
    }

    /**
     * Calculates how many days the loan is overdue.
     *
     * @param currentDate the current system date
     * @return number of overdue days, or 0 if not overdue
     */
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
