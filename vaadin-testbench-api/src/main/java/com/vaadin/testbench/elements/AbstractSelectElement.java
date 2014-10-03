/**
 * Copyright (C) 2012 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the license along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 */
package com.vaadin.testbench.elements;

import com.vaadin.testbench.elementsbase.ServerClass;

@ServerClass("com.vaadin.ui.AbstractSelect")
public class AbstractSelectElement extends AbstractFieldElement {

    /**
     * Clear operation is not supported for select element classes. This
     * operation has no effect on select element
     */
    @Override
    public void clear() {
        super.clear();
    }
}