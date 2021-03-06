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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.employeeroster;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;

import static java.util.Collections.singletonList;
import static org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Orientation.HORIZONTAL;

public class EmployeeRosterViewport extends Viewport<OffsetDateTime> {

    private final Integer tenantId;
    private final Supplier<EmployeeBlobView> blobViewSupplier;
    private final LinearScale<OffsetDateTime> scale;
    private final CssGridLines gridLines;
    private final Ticks<OffsetDateTime> ticks;
    private final List<Lane<OffsetDateTime>> lanes;

    EmployeeRosterViewport(final Integer tenantId,
                           final Supplier<EmployeeBlobView> blobViewSupplier,
                           final LinearScale<OffsetDateTime> scale,
                           final CssGridLines gridLines,
                           final Ticks<OffsetDateTime> ticks,
                           final List<Lane<OffsetDateTime>> lanes) {

        this.tenantId = tenantId;
        this.blobViewSupplier = blobViewSupplier;
        this.scale = scale;
        this.gridLines = gridLines;
        this.ticks = ticks;
        this.lanes = lanes;
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
                final String lowerDayOfTheWeek = date.getDayOfWeek().toString().toLowerCase();
                final String dayOfTheWeek = lowerDayOfTheWeek.substring(0, 1).toUpperCase() + lowerDayOfTheWeek.substring(1);
                int month = date.getMonth().getValue();
                return ((month < 10 ? "0" : "") + month) + "/" + date.getDayOfMonth() + " " + dayOfTheWeek.substring(0, 1);
            } else {
                return (hours < 10 ? "0" : "") + hours + ":00";
            }
        });
    }

    @Override
    public Lane<OffsetDateTime> newLane() {
        return new EmployeeLane(new Employee(tenantId, "New Employee"),
                new ArrayList<>(singletonList(new SubLane<>())));
    }

    @Override
    public Stream<Blob<OffsetDateTime>> newBlob(final Lane<OffsetDateTime> lane, final OffsetDateTime start) {

        // Casting is preferable to avoid over-use of generics in the Viewport class
        final EmployeeLane employeeLane = (EmployeeLane) lane;

        final EmployeeAvailability employeeAvailability = new EmployeeAvailability(tenantId, employeeLane.getEmployee(), GwtJavaTimeWorkaroundUtil.toLocalDate(start), start.toOffsetTime(), start.plusHours(8L)
                .toOffsetTime());

        return Stream.of(new EmployeeBlob(scale, employeeAvailability));
    }

    @Override
    public BlobView<OffsetDateTime, ?> newBlobView() {
        return blobViewSupplier.get();
    }

    @Override
    public List<Lane<OffsetDateTime>> getLanes() {
        return lanes;
    }

    @Override
    public Long getGridPixelSizeInScreenPixels() {
        return 20L;
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
