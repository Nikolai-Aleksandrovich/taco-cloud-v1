package sia5;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 14:12
 */
@Data
@Entity
@AllArgsConstructor
public class PurchaseOrder {
    @Id
    private String purchaseId;

    private String value;
    private String billingInfo;
    private String lineItems;

}
