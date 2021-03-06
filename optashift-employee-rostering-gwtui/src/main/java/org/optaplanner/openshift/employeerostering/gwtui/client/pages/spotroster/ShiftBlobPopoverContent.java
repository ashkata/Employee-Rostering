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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.Event;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobPopover;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobPopoverContent;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

import static java.lang.Long.parseLong;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;

@Templated
public class ShiftBlobPopoverContent implements BlobPopoverContent {

    @Inject
    @DataField("root")
    private HTMLDivElement root;

    @Inject
    @DataField("close-button")
    private HTMLButtonElement closeButton;

    @Inject
    @DataField("from-day")
    private HTMLInputElement fromDay;

    @Inject
    @DataField("from-hour")
    private HTMLInputElement fromHour;

    @Inject
    @DataField("to-day")
    private HTMLInputElement toDay;

    @Inject
    @DataField("to-hour")
    private HTMLInputElement toHour;

    @Inject
    @DataField("spot")
    private ListBox spotSelect; //FIXME: Don't use GWT widget

    @Inject
    @DataField("employee")
    private ListBox employeeSelect; //FIXME: Don't use GWT widget

    @Inject
    @DataField("pinned")
    private HTMLInputElement pinned;

    @Inject
    @Named("p")
    @DataField("rotation-employee")
    private HTMLElement rotationEmployee;

    @Inject
    @DataField("delete-button")
    private HTMLButtonElement deleteButton;

    @Inject
    @DataField("cancel-button")
    private HTMLButtonElement cancelButton;

    @Inject
    @DataField("apply-button")
    private HTMLButtonElement applyButton;

    @Inject
    private TenantStore tenantStore;

    private BlobPopover popover;

    private ShiftBlobView blobView;

    private Map<Long, Employee> employeesById;

    @Override
    public void init(final BlobView<?, ?> blobView) {

        this.blobView = (ShiftBlobView) blobView;
        final ShiftBlob blob = (ShiftBlob) blobView.getBlob();
        final Shift shift = blob.getShift();

        employeeSelect.clear();
        employeeSelect.addItem("Unassigned", "-1"); //FIXME: i18n

        // TODO: Do rest call
        SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(spots -> {
            spots.forEach(s -> this.spotSelect.addItem(s.getName(), s.getId().toString()));
            spotSelect.setSelectedIndex(spots.indexOf(shift.getSpot()));
        }));

        EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(employees -> {
            this.employeesById = employees.stream().collect(Collectors.toMap(Employee::getId, Function.identity()));
            employees.forEach(e -> employeeSelect.addItem(e.getName(), e.getId().toString()));
            employeeSelect.setSelectedIndex((shift.getEmployee() == null) ? 0 : employees.indexOf(shift.getEmployee()) + 1);
        }));

        final OffsetDateTime start = shift.getStartDateTime();
        fromDay.value = start.getMonth().toString() + " " + start.getDayOfMonth(); //FIXME: i18n
        fromHour.value = GwtJavaTimeWorkaroundUtil.toLocalTime(start) + "";

        final OffsetDateTime end = shift.getEndDateTime();
        toDay.value = end.getMonth().toString() + " " + end.getDayOfMonth(); //FIXME: i18n
        toHour.value = GwtJavaTimeWorkaroundUtil.toLocalTime(end) + "";

        pinned.checked = shift.isPinnedByUser();

        updateEmployeeSelect();
        rotationEmployee.textContent = Optional.ofNullable(shift.getRotationEmployee()).map(Employee::getName).orElse("-");
    }

    private void updateEmployeeSelect() {
        employeeSelect.setEnabled(pinned.checked);
    }

    @EventHandler("root")
    public void onClick(@ForEvent("click") final MouseEvent e) {
        e.stopPropagation();
    }

    @EventHandler("cancel-button")
    public void onCancelButtonClick(@ForEvent("click") final MouseEvent e) {
        popover.hide();
        e.stopPropagation();
    }

    @EventHandler("close-button")
    public void onCloseButtonClick(@ForEvent("click") final MouseEvent e) {
        popover.hide();
        e.stopPropagation();
    }

    @EventHandler("pinned")
    public void onPinnedCheckboxClick(@ForEvent("change") final Event e) {
        updateEmployeeSelect();
        e.stopPropagation();
    }

    @EventHandler("apply-button")
    public void onApplyButtonClick(@ForEvent("click") final MouseEvent e) {

        final ShiftBlob blob = (ShiftBlob) blobView.getBlob();
        final Shift shift = blob.getShift();

        final boolean oldLockedByUser = shift.isPinnedByUser();
        final Employee oldEmployee = shift.getEmployee();

        shift.setPinnedByUser(pinned.checked);
        shift.setEmployee(employeesById.get(parseLong(employeeSelect.getSelectedValue())));

        ShiftRestServiceBuilder.updateShift(shift.getTenantId(), new ShiftView(shift), onSuccess((final Shift updatedShift) -> {
            blob.setShift(updatedShift);
            blobView.refresh();
            popover.hide();
        }).onFailure(i -> {
            shift.setPinnedByUser(oldLockedByUser);
            shift.setEmployee(oldEmployee);
        }).onError(i -> {
            shift.setPinnedByUser(oldLockedByUser);
            shift.setEmployee(oldEmployee);
        }));

        e.stopPropagation();
    }

    @EventHandler("delete-button")
    public void onDeleteButtonClick(@ForEvent("click") final MouseEvent e) {
        blobView.remove();
        popover.hide();
        e.stopPropagation();
    }

    @Override
    public BlobPopoverContent withPopover(final BlobPopover popover) {
        this.popover = popover;
        return this;
    }
}
