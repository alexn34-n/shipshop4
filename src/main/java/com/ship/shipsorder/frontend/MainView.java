package com.ship.shipsorder.frontend;


import com.ship.shipsorder.TestVaadinSessionScope;
import com.ship.shipsorder.config.security.CustomPrincipal;
import com.ship.shipsorder.entity.Cart;
import com.ship.shipsorder.entity.Product;
import com.ship.shipsorder.entity.repository.CartRepository;
import com.ship.shipsorder.entity.repository.ProductRepository;
import com.ship.shipsorder.service.CartService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;
import java.util.stream.Collectors;

@Route
public class MainView extends VerticalLayout {
    private final Grid<Product> grid = new Grid<>(Product.class);

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final Authentication authentication;
    //private final TestVaadinSessionScope testVaadinSessionScope;


    public MainView(ProductRepository productRepository,
                    CartService cartService,
                    CartRepository cartRepository,
                    TestVaadinSessionScope testVaadinSessionScope) {
       // this.testVaadinSessionScope=testVaadinSessionScope;
        this.cartRepository=cartRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.authentication = SecurityContextHolder.getContext().getAuthentication();

        initPage();
       // VaadinSession.getCurrent().getSession().setMaxInactiveInterval(20);
    }

    private void initPage() {
        initProductGrid();
        initMainPage();
        //???? ???????????? ?????????? ?????????? ?????????????????? ????????, ???????????????? ???????????? ???????????? ?? ?????? ?? ???????? ?????? ????????
        /*if(SecurityUtils.isAdmin(authentication)) {
            add(initAddToCartButton());
        }*/
    }

    private void initMainPage() {
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(initFirstRow(), grid, initAddToCartButton());
    }

    private Component initFirstRow() {
        var toCartButton = new Button("??????????????", item -> {
            UI.getCurrent().navigate("cart");
        });

        var logoutButton = new Button("??????????", item -> {
            SecurityContextHolder.clearContext();
            UI.getCurrent().navigate("login");
        });

        return new HorizontalLayout(toCartButton, logoutButton);
    }

    private HorizontalLayout initAddToCartButton() {
        var addToCartButton = new Button("???????????????? ?? ??????????????", items -> {
        var optionalCart=cartRepository.findCartByOrderIdIsNull();
            Cart cart;
            if (optionalCart.isEmpty()) {
                cart = new Cart();
            cart.setId(UUID.randomUUID());
            cart.setUser(((CustomPrincipal)authentication.getPrincipal()).getUser());
            cart.setProductList(grid.getSelectedItems().stream().peek(p->p.setCount(1)).collect(Collectors.toSet()));
            }else{
                cart = optionalCart.get();
            cart.addToProductList(grid.getSelectedItems().stream().peek(p->p.setCount(1)).collect(Collectors.toSet()));
            }
            cartRepository.save(cart);
            Notification.show("?????????? ?????????????? ???????????????? ?? ??????????????");
        });

        return new HorizontalLayout(addToCartButton);
    }

    private void initProductGrid() {
        var products = productRepository.findAll();

        grid.setItems(products);
        grid.setColumns("name", "price", "count");
        grid.setSizeUndefined();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        ListDataProvider<Product> dataProvider = DataProvider.ofCollection(products);
        grid.setDataProvider(dataProvider);

        initPlusMinusButtons();
        initReviewsButtons();

    }

    private void initReviewsButtons() {
        grid.addColumn(new ComponentRenderer<>(item -> {
            var seeReviewButton = new Button("????????????", i -> {
                ComponentUtil.setData(UI.getCurrent(),"product",item);
            UI.getCurrent().navigate("review");
            });

            return new HorizontalLayout( seeReviewButton);
        }));
    }

    private void initPlusMinusButtons() {
        grid.addColumn(new ComponentRenderer<>(item -> {
            var plusButton = new Button("+", i -> {
                item.incrementCount();
                productRepository.save(item);
                grid.getDataProvider().refreshItem(item);
            });

            var minusButton = new Button("-", i -> {
                item.decreaseCount();
                productRepository.save(item);
                grid.getDataProvider().refreshItem(item);
            });

            return new HorizontalLayout(plusButton, minusButton);
        }));
    }
}