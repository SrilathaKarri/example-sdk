package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 *  Represents the state of a transaction involving appointment and care context creation and management.
 *  This class is used to track the status of various steps in a transaction process.
 *  It also supports serialization for storage or transmission.
 *  <p>
 *  The state includes flags indicating whether the appointment and care context were created,
 *  whether visit records were updated, and whether the care context was successfully linked.
 *  </p>
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"appointmentReference", "careContextReference", "requestId"})
public class TransactionState implements Serializable {
    /**
     * The reference ID of the appointment associated with this transaction.
     */
    private String appointmentReference;
    /**
     * The reference ID of the care context associated with this transaction.
     */
    private String careContextReference;
    /**
     * The unique request ID for this transaction.
     */
    private String requestId;
    /**
     * Flag indicating whether the appointment has been created.
     * Default value is {@code false}.
     */
    private boolean appointmentCreated = false;
    /**
     * Flag indicating whether the care context has been created.
     * Default value is {@code false}.
     */
    private boolean careContextCreated = false;
    /**
     * Flag indicating whether the visit records have been updated.
     * Default value is {@code false}.
     */
    private boolean visitRecordsUpdated = false;
    /**
     * Flag indicating whether the care context has been successfully linked.
     * Default value is {@code false}.
     */
    private boolean careContextLinked = false;

}
