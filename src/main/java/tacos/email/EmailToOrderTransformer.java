package tacos.email;

import org.springframework.integration.mail.transformer.AbstractMailMessageTransformer;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;
import tacos.domain.Ingredient;
import tacos.domain.Taco;
import tacos.email.domain.Order;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * <p>Handles email content as taco orders where...</p>
 *  <li> The order's email is the sender's email</li>
 *  <li> The email subject line *must* be "TACO ORDER" or else it will be ignored</li>
 *  <li> Each line of the email starts with the name of a taco design, followed by a colon,
 *    followed by one or more ingredient names in a comma-separated list.</li>
 *
 * <p>The ingredient names are matched against a known set of ingredients using a LevenshteinDistance
 * algorithm. As an example "beef" will match "GROUND BEEF" and be mapped to "GRBF"; "corn" will
 * match "Corn Tortilla" and be mapped to "COTO".</p>
 *
 * <p>An example email body might look like this:</p>
 *
 * <code>
 * Corn Carnitas: corn, carnitas, lettuce, tomatoes, cheddar<br/>
 * Veggielicious: flour, tomatoes, lettuce, salsa
 * </code>
 *
 * <p>This will result in an order with two tacos where the names are "Corn Carnitas" and "Veggielicious".
 * The ingredients will be {COTO, CARN, LETC, TMTO, CHED} and {FLTO,TMTO,LETC,SLSA}.</p>
 */

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 17:36
 */
@Component
//这是Spring Integration transformer接口的实现，扩展了AbstractMailMessageTransformer
public class EmailToOrderTransformer extends AbstractMailMessageTransformer<Order> {

    @Override
    protected AbstractIntegrationMessageBuilder<Order>
    doTransform(Message mailMessage) throws Exception {

        Order tacoOrder = processPayload(mailMessage);
        //首先，processPayload将电子邮件信息解析为Order对象

        return MessageBuilder.withPayload(tacoOrder);
    }
    private Order processPayload(Message mailMessage) {
        try {
            String subject = mailMessage.getSubject();
            if (subject.toUpperCase().contains(SUBJECT_KEYWORDS)) {
                String email =
                        ((InternetAddress) mailMessage.getFrom()[0]).getAddress();
                String content = mailMessage.getContent().toString();
                return parseEmailToOrder(email, content);
            }
        } catch (MessagingException e) {
        } catch (IOException e) {}
        return null;
    }
    private Order parseEmailToOrder(String email, String content) {
        Order order = new Order(email);
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().length() > 0 && line.contains(":")) {
                String[] lineSplit = line.split(":");
                String tacoName = lineSplit[0].trim();
                String ingredients = lineSplit[1].trim();
                String[] ingredientsSplit = ingredients.split(",");
                List<String> ingredientCodes = new ArrayList<>();
                for (String ingredientName : ingredientsSplit) {
                    String code = lookupIngredientCode(ingredientName.trim());
                    if (code != null) {
                        ingredientCodes.add(code);
                    }
                }

                Taco taco = new Taco(tacoName);
                taco.setIngredients(ingredientCodes);
                order.addTaco(taco);
            }
        }
        return order;
    }
    private String lookupIngredientCode(String ingredientName) {
        for (Ingredient ingredient : ALL_INGREDIENTS) {
            String ucIngredientName = ingredientName.toUpperCase();
            if (LevenshteinDistance.getDefaultInstance().apply(ucIngredientName, ingredient.getName()) < 3 ||
                    ucIngredientName.contains(ingredient.getName()) ||
                    ingredient.getName().contains(ucIngredientName)) {
                return ingredient.getCode();
            }
        }
        return null;
    }
    private static Ingredient[] ALL_INGREDIENTS = new Ingredient[] {
            new Ingredient("FLTO", "FLOUR TORTILLA"),
            new Ingredient("COTO", "CORN TORTILLA"),
            new Ingredient("GRBF", "GROUND BEEF"),
            new Ingredient("CARN", "CARNITAS"),
            new Ingredient("TMTO", "TOMATOES"),
            new Ingredient("LETC", "LETTUCE"),
            new Ingredient("CHED", "CHEDDAR"),
            new Ingredient("JACK", "MONTERREY JACK"),
            new Ingredient("SLSA", "SALSA"),
            new Ingredient("SRCR", "SOUR CREAM")

    };
}
