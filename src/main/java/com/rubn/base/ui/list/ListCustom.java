package com.rubn.base.ui.list;

import com.rubn.base.ui.Layout;
import com.rubn.base.ui.utility.Color;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.theme.lumo.LumoUtility.ListStyleType;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

public class ListCustom extends com.vaadin.flow.component.html.UnorderedList implements HasTheme {

    // Style
    private Color.Background background;
    private Layout.Display display;
    private Layout.FlexWrap flexWrap;
    private Layout.ColumnGap colGap;
    private Layout.RowGap rowGap;
    private Layout.Overflow overflow;

    public ListCustom() {
        addClassNames(ListStyleType.NONE, Margin.Vertical.NONE, Padding.Start.NONE);
        setDisplay(Layout.Display.GRID);
        getStyle().setAlignItems(Style.AlignItems.START);
        getStyle().setFlexDirection(Style.FlexDirection.ROW_REVERSE);
        getStyle().setJustifyContent(Style.JustifyContent.START);
    }

    public void add(Component... components) {
        super.add(components);
    }

    /**
     * Sets auto-sizing for grid columns with a minimum width for each column.
     */
    public void setAutoFill(float width, Unit unit) {
        getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(" + width + unit.toString() + ", 1fr))");
    }

    /**
     * Removes the background color.
     */
    public void removeBackgroundColor() {
        if (this.background != null) {
            this.removeClassName(this.background.getClassName());
        }
        this.background = null;
    }

    /**
     * Sets the display property.
     */
    public void setDisplay(Layout.Display display) {
        if (this.display != null) {
            removeClassNames(this.display.getClassName());
        }
        addClassNames(display.getClassName());
        this.display = display;
    }

    public void setFlexWrap(Layout.FlexWrap flexWrap) {
        if (this.flexWrap != null) {
            removeClassNames(this.flexWrap.getClassName());
        }
        addClassNames(flexWrap.getClassName());
        this.flexWrap = flexWrap;
    }

    /**
     * Sets both the column (horizontal) and row (vertical) gap between components.
     */
    public void setGap(Layout.Gap gap) {
        setColumnGap(gap);
        setRowGap(gap);
    }

    /**
     * Sets the column (horizontal) gap between components.
     */
    public void setColumnGap(Layout.Gap gap) {
        removeColumnGap();
        this.addClassNames(gap.getColumnGap().getClassName());
        this.colGap = gap.getColumnGap();
    }

    /**
     * Sets the row (vertical) gap between components.
     */
    public void setRowGap(Layout.Gap gap) {
        removeRowGap();
        this.addClassNames(gap.getRowGap().getClassName());
        this.rowGap = gap.getRowGap();
    }

    /**
     * Removes both the column (horizontal) and row (vertical) gap between components.
     */
    public void removeGap() {
        removeColumnGap();
        removeRowGap();
    }

    /**
     * Removes the column (horizontal) gap between components.
     */
    public void removeColumnGap() {
        if (this.colGap != null) {
            this.removeClassName(this.colGap.getClassName());
        }
        this.colGap = null;
    }

    /**
     * Removes the row (vertical) gap between components.
     */
    public void removeRowGap() {
        if (this.rowGap != null) {
            this.removeClassName(this.rowGap.getClassName());
        }
        this.rowGap = null;
    }

    /**
     * Sets the overflow property.
     */
    public void setOverflow(Layout.Overflow overflow) {
        if (this.overflow != null) {
            removeClassNames(this.overflow.getClassName());
        }
        addClassNames(overflow.getClassName());
        this.overflow = overflow;
    }

}
