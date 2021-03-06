/*
 * Copyright 2011 Vaadin Ltd.
 *
 * Licensed under the GNU Affero General Public License, Version 3.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/agpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.graph.client;

import org.vaadin.gwtgraphics.client.Line;

import com.google.gwt.dom.client.*;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;

/**
 * Presenter/controller for an arc in a graph.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
class ArcPresenter implements Controller {

    private static final int ARROWHEAD_LENGTH = 10;
    private static final int ARROWHEAD_WIDTH = ARROWHEAD_LENGTH / 2;

    private final VGraphExplorer parent;
    private final ArcProxy model;
    private final Line viewBody = new Line(0, 0, 0, 0);
    private final HTML viewLabel;
    private final Line viewHeadLeft = new Line(0, 0, 0, 0);
    private final Line viewHeadRight = new Line(0, 0, 0, 0);

    private double headX;
    private double headY;

    ArcPresenter(VGraphExplorer parent, ArcProxy model) {
        this.parent = parent;
        this.model = model;

        parent.add(viewBody);
        parent.add(viewHeadLeft);
        parent.add(viewHeadRight);

        viewLabel = new HTML(model.getLabel());
        viewLabel.getElement().setClassName("arc");
        if (!model.isGroup()) {
            viewLabel.setTitle(model.getId());
        }
        parent.add(viewLabel);

        onUpdateInModel();
    }

    private static double distance(double fromX, double fromY, double toX,
                                   double toY) {
        return Math.abs(toX - fromX) + Math.abs(toY - fromY);
    }

    public void onRemoveFromModel() {
        model.setController(null);
        parent.remove(viewBody);
        parent.remove(viewLabel);
        parent.remove(viewHeadLeft);
        parent.remove(viewHeadRight);
    }

    public void onUpdateInModel() {
        updateLine();
        updateLabel();
        updateArrowhead();
    }

    private void updateArrowhead() {
        GraphProxy graph = parent.getGraph();
        NodeProxy from = graph.getTail(model);
        double fromX = from.getX();
        double fromY = from.getY();
        NodeProxy to = graph.getHead(model);
        double toX = to.getX();
        double toY = to.getY();
        double dX = toX - fromX;
        double dY = toY - fromY;
        headX = toX;
        headY = toY;
        double distance = distance(fromX, fromY, toX, toY);
        double newX;
        double newY;

        double halfWidth = to.getWidth() / 2.0;
        double left = toX - halfWidth;
        double right = toX + halfWidth;
        newX = fromX < left ? left : fromX > right ? right : fromX;
        newY = fromY + dY * (newX - fromX) / dX;
        double newDistance = distance(newX, newY, toX, toY);
        if (newDistance < distance) {
            distance = newDistance;
            headX = newX;
            headY = newY;
        }

        double halfHeight = to.getHeight() / 2.0;
        double top = toY - halfHeight;
        double bottom = toY + halfHeight;
        newY = fromY < top ? top : fromY > bottom ? bottom : fromY;
        newX = fromX + dX * (newY - fromY) / dY;
        if (distance(newX, newY, toX, toY) < distance) {
            headX = newX;
            headY = newY;
        }

        double angle = Math.atan2(dY, dX);
        double leftX = headX
                       + rotateX(-ARROWHEAD_LENGTH, -ARROWHEAD_WIDTH, angle);
        double leftY = headY
                       + rotateY(-ARROWHEAD_LENGTH, -ARROWHEAD_WIDTH, angle);
        updateLine(viewHeadLeft, headX, headY, leftX, leftY);

        double rightX = headX
                        + rotateX(-ARROWHEAD_LENGTH, ARROWHEAD_WIDTH, angle);
        double rightY = headY
                        + rotateY(-ARROWHEAD_LENGTH, ARROWHEAD_WIDTH, angle);
        updateLine(viewHeadRight, headX, headY, rightX, rightY);
    }

    private void updateLine() {
        GraphProxy graph = parent.getGraph();
        NodeProxy from = graph.getTail(model);
        NodeProxy to = graph.getHead(model);
        updateLine(viewBody, from.getX(), from.getY(), to.getX(), to.getY());
    }

    private Style updateLabel() {
        Style style = viewLabel.getElement().getStyle();
        GraphProxy graph = parent.getGraph();
        NodeProxy from = graph.getTail(model);

        double x = getLabelCenter(from.getX(), headX)
                   - viewLabel.getOffsetWidth() / 2.0;
        style.setLeft(x, Unit.PX);
        double y = getLabelCenter(from.getY(), headY)
                   - viewLabel.getOffsetHeight() / 2.0;
        style.setTop(y, Unit.PX);

        return style;
    }

    private static void updateLine(Line line, double x1, double y1, double x2,
                                   double y2) {
        updateLine(line, (int) Math.round(x1), (int) Math.round(y1),
                   (int) Math.round(x2), (int) Math.round(y2));
    }

    private static void updateLine(Line line, int x1, int y1, int x2, int y2) {
        line.setX1(x1);
        line.setY1(y1);
        line.setX2(x2);
        line.setY2(y2);
    }

    private static double getLabelCenter(double from, double to) {
        return .2 * from + .8 * to;
    }

    private static double rotateX(double x, double y, double angle) {
        return x * Math.cos(angle) - y * Math.sin(angle);
    }

    private static double rotateY(double x, double y, double angle) {
        return x * Math.sin(angle) + y * Math.cos(angle);
    }
}
