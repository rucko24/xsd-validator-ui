package com.rubn.xsdvalidator.view.list;

import com.rubn.xsdvalidator.util.Color;
import com.rubn.xsdvalidator.util.Layout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasTheme;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CLICK_LIKE_HORIZONTAL_SCROLL;

public class CustomList extends com.vaadin.flow.component.html.UnorderedList implements HasTheme {

    // Style
    private Color.Background background;
    private Layout.Display display;
    private Layout.FlexWrap flexWrap;
    private Layout.ColumnGap colGap;
    private Layout.RowGap rowGap;
    private Layout.Overflow overflow;

    public CustomList() {
        addClassName("custom-list-scroll");
        getElement().executeJs(CLICK_LIKE_HORIZONTAL_SCROLL);
    }

    public void add(Component... components) {
        super.add(components);
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
