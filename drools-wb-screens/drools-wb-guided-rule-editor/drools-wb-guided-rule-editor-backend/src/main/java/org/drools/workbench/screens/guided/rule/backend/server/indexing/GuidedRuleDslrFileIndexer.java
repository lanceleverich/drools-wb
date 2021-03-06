/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.drools.workbench.screens.guided.rule.backend.server.indexing;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.drools.compiler.lang.Expander;
import org.drools.compiler.lang.dsl.DSLMappingFile;
import org.drools.compiler.lang.dsl.DSLTokenizedMappingFile;
import org.drools.compiler.lang.dsl.DefaultExpander;
import org.drools.workbench.screens.guided.rule.type.GuidedRuleDSLRResourceTypeDefinition;
import org.guvnor.common.services.backend.file.FileDiscoveryService;
import org.kie.soup.project.datamodel.oracle.ProjectDataModelOracle;
import org.kie.workbench.common.services.backend.file.DSLFileFilter;
import org.kie.workbench.common.services.datamodel.backend.server.service.DataModelService;
import org.kie.workbench.common.services.refactoring.backend.server.indexing.DefaultIndexBuilder;
import org.kie.workbench.common.services.refactoring.backend.server.indexing.drools.AbstractDrlFileIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.java.nio.file.Path;

@ApplicationScoped
public class GuidedRuleDslrFileIndexer extends AbstractDrlFileIndexer {

    private static final Logger logger = LoggerFactory.getLogger(GuidedRuleDslrFileIndexer.class);

    private static final DSLFileFilter FILTER_DSLS = new DSLFileFilter();

    @Inject
    private DataModelService dataModelService;

    @Inject
    private FileDiscoveryService fileDiscoveryService;

    @Inject
    private GuidedRuleDSLRResourceTypeDefinition dslrType;

    @Override
    public boolean supportsPath(final Path path) {
        return dslrType.accept(Paths.convert(path));
    }

    @Override
    public DefaultIndexBuilder fillIndexBuilder(final Path path) throws Exception {
        final String dslr = ioService.readAllString(path);

        final Expander expander = getDSLExpander(path);
        final String drl = expander.expand(dslr);

        return fillDrlIndexBuilder(path, drl);
    }

    /**
     * Returns an expander for DSLs (only if there is a DSL configured for this package).
     *
     * @param path The {@link Path} of the resource to index
     * @return a {@link Expander} used to produce a DRL of the file
     */
    public Expander getDSLExpander(final Path path) {
        final Expander expander = new DefaultExpander();
        final List<DSLMappingFile> dsls = getDSLMappingFiles(path);
        for (DSLMappingFile dsl : dsls) {
            expander.addDSLMapping(dsl.getMapping());
        }
        return expander;
    }

    private List<DSLMappingFile> getDSLMappingFiles(final Path path) {
        final List<DSLMappingFile> dsls = new ArrayList<DSLMappingFile>();
        final org.uberfire.backend.vfs.Path vfsPath = Paths.convert(path);
        final org.uberfire.backend.vfs.Path packagePath = projectService.resolvePackage(vfsPath).getPackageMainResourcesPath();
        final Path nioPackagePath = Paths.convert(packagePath);
        final Collection<Path> dslPaths = fileDiscoveryService.discoverFiles(nioPackagePath,
                                                                             FILTER_DSLS);
        for (final Path dslPath : dslPaths) {
            final String dslDefinition = ioService.readAllString(dslPath);
            final DSLTokenizedMappingFile dslFile = new DSLTokenizedMappingFile();
            try {
                if (dslFile.parseAndLoad(new StringReader(dslDefinition))) {
                    dsls.add(dslFile);
                } else {
                    logger.error("Unable to parse DSL definition: " + dslDefinition);
                }
            } catch (IOException ioe) {
                logger.error(ioe.getMessage());
            }
        }
        return dsls;
    }

    /*
     * (non-Javadoc)
     * @see org.kie.workbench.common.services.refactoring.backend.server.indexing.drools.AbstractDrlFileIndexer#getProjectDataModelOracle(org.uberfire.java.nio.file.Path)
     */
    @Override
    protected ProjectDataModelOracle getProjectDataModelOracle(final Path path) {
        return dataModelService.getProjectDataModel(Paths.convert(path));
    }
}
