/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.workbench.services.verifier.core.checks.base;

import org.drools.workbench.services.verifier.api.client.configuration.AnalyzerConfiguration;
import org.drools.workbench.services.verifier.core.cache.inspectors.RuleInspector;

/**
 * A check that compares a row to another.
 */
public abstract class PairCheck
        extends CheckBase {

    protected final RuleInspector ruleInspector;
    protected final RuleInspector other;

    public PairCheck( final RuleInspector ruleInspector,
                      final RuleInspector other,
                      final AnalyzerConfiguration configuration ) {
        super( configuration );

        this.ruleInspector = ruleInspector;
        this.other = other;
    }

}

