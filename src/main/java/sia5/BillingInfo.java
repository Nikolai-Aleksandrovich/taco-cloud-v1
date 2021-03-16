package sia5;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Id;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 14:24
 */
@Data
@AllArgsConstructor
public class BillingInfo {
    @Id
    private Long id;

    private String name;

}
