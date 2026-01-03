package com.rubn.base.ui;

import com.rubn.xsdvalidator.records.AppVersionRecord;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Layout
public final class MainLayout extends AppLayout {

    private final AppVersionRecord appVersionRecord;

    public MainLayout(AppVersionRecord appVersionRecord) {
        this.appVersionRecord = appVersionRecord;
        setPrimarySection(Section.DRAWER);
        addToDrawer(createHeader(), new Scroller(createSideNav()));
    }

    private Component createHeader() {
        final Image logo = new Image("logo.png", "logo");
        logo.addClassName("logo");
        Tooltip.forComponent(logo).setText("https://github.com/rucko24/xsd-validator-ui");
        logo.getStyle().setCursor("pointer");
        logo.setWidth("50%");
        logo.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.getPage().open("https://github.com/rucko24/xsd-validator-ui"));
        });

        final Span spanName = new Span(appVersionRecord.version());
        spanName.getElement().getThemeList().add("badge pill contrast");
        spanName.getStyle().setBoxShadow(Constants.VAR_CUSTOM_BOX_SHADOW);
        spanName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.XXSMALL,
                LumoUtility.TextColor.SECONDARY);

        var header = new VerticalLayout(logo, spanName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }
}
