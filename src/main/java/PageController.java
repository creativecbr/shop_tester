import com.github.javafaker.Faker;
import consts.Consts;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.text.SimpleDateFormat;
import java.util.*;


public class PageController {

    private WebDriver driver;

    public PageController() {
        this.driver = new ChromeDriver();
    }

    /**
     * Adding products to bag/cart. Quantity of products specified in Consts.AMOUNT_OF_PRODUCTS.
     *
     * @param quantity quantity of products.
     */
    public void addProductsToBag(Integer quantity) {
        driver.get(Consts.SITE);
        setOffAlert();

        ArrayList<String> addresses = getCategoriesAddresses();
        if (!addresses.isEmpty()) {

            for (int i = 0; i < quantity; i++) {
                String address = addresses.get(i % (addresses.size()));
                if (!addProductToBag(address)) {
                    addresses.set(i % (addresses.size()), "https://3.209.82.53/14-szkla-hartowane");
                    i--;
                }
            }
        }
    }

    /**
     * Adding one product to bag in specified category, included random variants, random quantity, random page of products in category.
     *
     * @param address specified address of category of products.
     * @return confirmation if operation was succesfull.
     */
    private Boolean addProductToBag(String address) {
        driver.get(address);

        int maxPage = getLastPageNumber();
        if (maxPage > 0) {
            try {
                goToRandomProductsPage(maxPage);
            } catch (Exception e) {
                return false;
            }

            List<WebElement> products = driver.findElements(By.xpath("//div[@class='thumbnail-container']/a"));
            int product = getRandomInt(0, products.size() - 1);
            products.get(product).click();
            getRandomVariants();

            getRandomQuantity();

            addToBag();
            return true;
        } else
            return false;
    }

    /**
     * Adding personalized product to bag, and closing popup with information about order.
     */
    private void addToBag() {
        WebElement button = driver.findElement(By.xpath("//button[@data-button-action='add-to-cart']"));
        button.click();

        WebElement closeButton = new WebDriverWait(driver, 2)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='modal-header']/button[@class='close']")));
        closeButton.click();

    }


    /**
     * Putting random quantity of product into input field. Between 1 and 5 products.
     */
    private void getRandomQuantity() {

        WebElement element = driver.findElement(By.xpath("//input[@id='quantity_wanted']"));
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        String argument = "";
        if(Consts.QUANTITY_OF_BUYING_PRODUCTS == 1)
        {
            argument = "arguments[0].value='1';";
        }
        else
        {
            argument = "arguments[0].value='" + getRandomInt(1, Consts.QUANTITY_OF_BUYING_PRODUCTS+1).toString() + "';";
        }
        jse.executeScript(argument, element);
    }

    /**
     * Checking if variant exist, and choosing them randomly.
     */
    private void getRandomVariants() {

        // ul li variants
        if (driver.findElements(By.xpath("//div[@class='product-variants']/div/ul")).size() > 0)
        {
            List<WebElement> containersVariant = driver.findElements(By.xpath("//div[@class='product-variants']/div/ul"));
            for (WebElement e : containersVariant) {
                List<WebElement> variants = e.findElements(By.xpath("li"));
                variants.get(getRandomInt(0, variants.size())).click();
            }
        }

        // option variants
        if(driver.findElements(By.xpath("//div[@class='product-variants']/div/select")).size() > 0)
        {
            Select dropdown = new Select(driver.findElement(By.xpath("//div[@class='product-variants']/div/select")));
            int amount = driver.findElements(By.xpath("//div[@class='product-variants']/div/select/option")).size();
            dropdown.selectByIndex(getRandomInt(0, amount));
        }
    }

    /**
     * Carry out driver to random products page in category.
     *
     * @param amount maximal page number.
     */
    private void goToRandomProductsPage(int amount) {

        int pageNo = getRandomInt(1, amount);
        driver.get(driver.getCurrentUrl() + Consts.PAGE_PARAM + pageNo);
    }

    /**
     * Generate random int in specified range a...b
     *
     * @param a the beggining of range
     * @param b the end of range
     * @return Integer in range a...b
     */
    private Integer getRandomInt(int a, int b) {

        if (a >= b)
            return 0;
        return
                new Random().ints(a, b).findFirst().getAsInt();
    }

    /**
     * Seek for last page number in category.
     *
     * @return Last page number.
     */
    private int getLastPageNumber() {

        try {
            WebElement pagination = driver.findElement(By.className("pagination"));
            List<WebElement> paginations = pagination.findElements(By.tagName("a"));
            return Integer.parseInt(paginations.get(paginations.size() - 2).getText());
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * Turning off allert of unknown certificate.
     */
    private void setOffAlert() {

        if (driver.findElements(By.id("details-button")).size() > 0) {
            driver.findElement(By.id("details-button")).click();
            driver.findElement(By.id("proceed-link")).click();

        }

    }

    /**
     * Seeks for all existing categories and returning ArrayList which include addresses, quantity of addresses specifies by Consts.AMOUNT_OF_CATEGORIES.
     *
     * @return ArrayList of string with addresses.
     */
    private ArrayList<String> getCategoriesAddresses() {

        ArrayList<String> addressList = new ArrayList<>();
        try {
            WebElement content = driver.findElement(By.id("top-menu"));
            List<WebElement> parentCategories = content.findElements(By.xpath("//li/a[@data-depth='0']"));
            List<WebElement> categories = new ArrayList<>();
            List<WebElement> childCategories;
            for (WebElement c : parentCategories) {
                childCategories = c.findElements(By.xpath("../*/ul[@data-depth='1']/li/a"));

                if (childCategories.isEmpty()) {
                    categories.add(c);
                } else {
                    categories.addAll(childCategories);
                }
            }

            if (categories.size() < Consts.AMOUNT_OF_CATEGORIES) {
                throw new Exception();
            } else {
                for (int i = 0; i < Consts.AMOUNT_OF_CATEGORIES; i++) {
                    int catNo = new Random().ints(0, categories.size() - 1).findFirst().getAsInt();
                    addressList.add(categories.get(catNo).getAttribute("href"));
                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage() + " - ERROR UNTIL GETING PRODUCT ADRESS.");
        }

        return addressList;
    }

    /**
     * Deleting specified quantity of products from cart.
     *
     * @param quantityOfRemovingProducts quantity of deleting products.
     */
    public void deleteProductsFromBag(Integer quantityOfRemovingProducts) {

        goToCart();
        List<WebElement> deleteButtons = driver.findElements(By.xpath("//a[@class='remove-from-cart']"));
        for (int i = 0; i < Consts.QUANTITY_OF_REMOVING_PRODUCTS; i++) {
            deleteButtons.get(getRandomInt(0, deleteButtons.size() - 1)).click();
        }

    }

    /**
     * Goes to cart from any page.
     */
    private void goToCart() {
        WebElement div_cart = driver.findElement(By.id("_desktop_cart"));
        String cartAddress = div_cart.findElement(By.tagName("a")).getAttribute("href");
        driver.get(cartAddress);
    }

    /**
     * Conducts order with creating a new account. Include choosing one of the delivery options and one of the payment options and confirm order.
     */
    public void realizeOrderWithCreatingAccount() {

        goToCart();
        driver.findElement(By.xpath("//div[@class='checkout cart-detailed-actions card-block']/div/a")).click();

        Actions actions = new Actions(driver);
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        Faker faker = new Faker();


        List<WebElement> genders = driver.findElements(By.xpath("//input[@name='id_gender']"));
        WebElement name = driver.findElement(By.xpath("//input[@name='firstname']"));
        WebElement surname = driver.findElement(By.xpath("//input[@name='lastname']"));
        WebElement email = driver.findElement(By.xpath("//input[@name='email']"));
        WebElement password = driver.findElement(By.xpath("//input[@name='password']"));
        WebElement birthday = driver.findElement(By.xpath("//input[@name='birthday']"));
        WebElement conditionals = driver.findElement(By.xpath("//input[@name='psgdpr']"));
        WebElement customerPrivacy = driver.findElement(By.xpath("//input[@name='customer_privacy']"));
        WebElement next = driver.findElement(By.xpath("//button[@name='continue']"));

        //Gender choosing
        actions.moveToElement(genders.get(getRandomInt(0, genders.size()))).click().perform();

        //Putting name
        name.sendKeys(faker.name().firstName());

        //Putting surnname
        surname.sendKeys(faker.name().lastName());

        //Putting email
        email.sendKeys(faker.internet().emailAddress());

        //Putting password
        password.sendKeys(faker.internet().password());

        //Putting birthday
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = faker.date().birthday();
        String stringDate = format.format(date);
        birthday.sendKeys(stringDate);

        //Conditionals accepting
        actions.moveToElement(conditionals).click().perform();

        //Customer's privacy accepting
        actions.moveToElement(customerPrivacy).click().perform();

        //Next Page
        next.click();

        //Getting inputs on next page
        WebElement addressStreet = driver.findElement(By.xpath("//input[@name='address1']"));
        WebElement addressNumber = driver.findElement(By.xpath("//input[@name='address2']"));
        WebElement postcode = driver.findElement(By.xpath("//input[@name='postcode']"));
        WebElement city = driver.findElement(By.xpath("//input[@name='city']"));
        WebElement phoneNumber = driver.findElement(By.xpath("//input[@name='phone']"));
        next = driver.findElement(By.xpath("//button[@name='confirm-addresses']"));

        //Putting address street
        addressStreet.sendKeys(faker.address().streetAddress());

        //Putting address number
        addressNumber.sendKeys(faker.address().streetAddressNumber());

        //Putting postcode
        postcode.sendKeys(faker.numerify("##-###"));

        //Putting city
        city.sendKeys(faker.address().city());

        //Putting phone number
        phoneNumber.sendKeys(faker.numerify("+48#########"));

        //Next step button
        next.click();

        //Getting suppliers input and choosing random one
        List<WebElement> suppliers = driver.findElements(By.xpath("//input[contains(@name, 'delivery_option')]"));
        actions.moveToElement(suppliers.get(getRandomInt(0, suppliers.size()))).click().perform();

        //Getting confirm delivery button and click
        next = driver.findElement(By.xpath("//button[@name='confirmDeliveryOption']"));
        next.click();

        // Choosing payment on delivery
        WebElement payment = driver.findElement(By.xpath("//input[@data-module-name='ps_cashondelivery']"));
        actions.moveToElement(payment).click().perform();

        //Accept conditionals
        conditionals = driver.findElement(By.xpath("//input[@name='conditions_to_approve[terms-and-conditions]']"));
        actions.moveToElement(conditionals).click().perform();

        //Confirm button
        WebElement confirmButton = new WebDriverWait(driver, 2)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='payment-confirmation']/div/button")));
        confirmButton.click();


    }

    /**
     * If new user is logged in, go to account.
     */
    private void goToAccount() {

        WebElement account = driver.findElement(By.xpath("//div[@class='user-info']/a[@class='account']"));
        account.click();

    }

    /**
     * Goes to account and check history of last order.
     */
    public void checkOrderStatusOnNewAccount() {
        goToAccount();

        //Go to history link
        WebElement history = driver.findElement(By.xpath("//a[@id='history-link']"));
        history.click();

        //Go to status of one order
        WebElement status = driver.findElement(By.xpath("//a[@data-link-action='view-order-details']"));
        status.click();
    }
}
