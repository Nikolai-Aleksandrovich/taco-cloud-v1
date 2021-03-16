package sia5;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Id;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 14:48
 */
@Data
@AllArgsConstructor
public class LineItem {
    @Id
    private Long itemId;
    private String name;
}
