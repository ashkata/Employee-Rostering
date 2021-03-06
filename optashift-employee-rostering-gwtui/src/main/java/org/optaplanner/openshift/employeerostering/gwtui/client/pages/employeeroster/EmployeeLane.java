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
import java.util.List;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;

public class EmployeeLane extends Lane<OffsetDateTime> {

    private final Employee employee;

    // Changing the parameter name to 'availabilityState' leads to an error, because GWT compiler uses the field instead of the parameter.
    public EmployeeLane(final Employee employeeParam,
                        final List<SubLane<OffsetDateTime>> subLanes) {

        super(employeeParam.toString(), subLanes);
        this.employee = employeeParam;
    }

    public Employee getEmployee() {
        return employee;
    }
}
