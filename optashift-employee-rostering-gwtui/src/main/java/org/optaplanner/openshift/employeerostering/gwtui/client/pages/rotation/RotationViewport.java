/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.rotation;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.CssGridLines;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.Ticks;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Orientation;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

import static java.util.Collections.singletonList;
import static org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Orientation.HORIZONTAL;

public class RotationViewport extends Viewport<OffsetDateTime> {

    private final Integer tenantId;
    private final OffsetDateTime baseDate;
    private final Supplier<ShiftBlobView> blobViewFactory;
    private final LinearScale<OffsetDateTime> scale;
    private final CssGridLines gridLines;
    private final Ticks<OffsetDateTime> ticks;
    private final List<Lane<OffsetDateTime>> lanes;

    RotationViewport(final Integer tenantId,
                     final OffsetDateTime baseDate,
                     final Supplier<ShiftBlobView> blobViewFactory,
                     final LinearScale<OffsetDateTime> scale,
                     final CssGridLines gridLines,
                     final Ticks<OffsetDateTime> ticks,
                     final List<Lane<OffsetDateTime>> lanes) {

        this.tenantId = tenantId;
        this.baseDate = baseDate;
        this.blobViewFactory = blobViewFactory;
        this.scale = scale;
        this.ticks = ticks;
        this.lanes = lanes;
        this.gridLines = gridLines;
    }

    @Override
    public void drawGridLinesAt(final IsElement target) {
        gridLines.drawAt(target, this);
    }

    @Override
    public void drawTicksAt(final IsElement target) {
        //FIXME: Make it18n
        ticks.drawAt(target, this, date -> {
            final int hours = date.getHour();
            if (hours == 0) {
                return "Day " + (Duration.between(scale.toScaleUnits(0L), date).getSeconds() / 60 / 60 / 24);
            } else {
                return (hours < 10 ? "0" : "") + hours + ":00";
            }
        });
    }

    @Override
    public Lane<OffsetDateTime> newLane() {
        return new SpotLane(new Spot(tenantId, "New spot", new HashSet<>()),
                new ArrayList<>(singletonList(new SubLane<>())));
    }

    @Override
    public Stream<Blob<OffsetDateTime>> newBlob(final Lane<OffsetDateTime> lane,
                                                final OffsetDateTime positionInScaleUnits) {

        final SpotLane spotLane = (SpotLane) lane;

        final Shift newShift = new Shift(
                tenantId,
                spotLane.getSpot(),
                positionInScaleUnits,
                positionInScaleUnits.plusHours(8L));

        return new ShiftBlob(newShift, baseDate, scale).toStream();
    }

    @Override
    public BlobView<OffsetDateTime, ?> newBlobView() {
        return blobViewFactory.get();
    }

    @Override
    public List<Lane<OffsetDateTime>> getLanes() {
        return lanes;
    }

    @Override
    public Long getGridPixelSizeInScreenPixels() {
        return 10L;
    }

    @Override
    public Orientation getOrientation() {
        return HORIZONTAL;
    }

    @Override
    public LinearScale<OffsetDateTime> getScale() {
        return scale;
    }
}
