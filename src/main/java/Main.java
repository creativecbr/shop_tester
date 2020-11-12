import consts.Consts;

public class Main {


    public static void main(String[] args) {
        PageController controller = new PageController();

        controller.addProductsToBag(Consts.AMOUNT_OF_PRODUCTS);
        controller.deleteProductsFromBag(Consts.QUANTITY_OF_REMOVING_PRODUCTS);
        controller.realizeOrderWithCreatingAccount();
        controller.checkOrderStatusOnNewAccount();
    }

}
