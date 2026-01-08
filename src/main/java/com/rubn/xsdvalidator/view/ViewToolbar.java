package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ViewToolbar extends Composite<HorizontalLayout> {

    public ViewToolbar(@Nullable String viewTitle, Component... components) {
        var layout = getContent();
        layout.setPadding(false);
        layout.setWrap(true);
        layout.setWidthFull();
        layout.addClassName(LumoUtility.Border.BOTTOM);

        var drawerToggle = new DrawerToggle();
        drawerToggle.addClassNames(LumoUtility.Margin.NONE);

        var title = new H1(viewTitle);
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE, LumoUtility.FontWeight.LIGHT);

        var toggleAndTitle = new HorizontalLayout(drawerToggle, title);
        toggleAndTitle.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.add(toggleAndTitle);
        layout.setFlexGrow(1, toggleAndTitle);

        if (components.length > 0) {
            var actions = new HorizontalLayout(components);
            actions.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            layout.add(actions);
        }
    }

    public static Component group(Component... components) {
        HorizontalLayout group = new HorizontalLayout();
        if(Objects.nonNull(components)) {
            group.add(components);
        }
        group.setWrap(true);
        return group;
    }
}
