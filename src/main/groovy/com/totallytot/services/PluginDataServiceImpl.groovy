package com.totallytot.services

import com.atlassian.activeobjects.external.ActiveObjects
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport
import com.atlassian.sal.api.transaction.TransactionCallback
import com.totallytot.ao.IgnoredSpace
import com.totallytot.ao.MonitoredGroup

import javax.inject.Inject
import javax.inject.Named
import java.sql.SQLException

import org.apache.log4j.Logger

import static com.google.common.base.Preconditions.checkNotNull

@ExportAsService([PluginDataService])
@Named("pluginDataService")
class PluginDataServiceImpl implements PluginDataService{

    private static final Logger log = Logger.getLogger(PluginDataServiceImpl.class)

    @ComponentImport
    private final ActiveObjects ao

    @Inject
    PluginDataServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao)
    }

    @Override
    void setIgnoredSpace(String spaceKey) {
        ao.executeInTransaction({
            final IgnoredSpace ignoredSpace = ao.create(IgnoredSpace.class)
            ignoredSpace.setIgnoredSpaceKey(spaceKey)
            ignoredSpace.save()
            ignoredSpace
        })
    }

    @Override
    Set<String> getIgnoredSpaces() {
        def spaces = [] as Set
        ao.executeInTransaction((TransactionCallback<Void>) {
            ao.find(IgnoredSpace.class).each {spaces << it.ignoredSpaceKey}
            null
        })
        spaces
    }

    @Override
    void removeIgnoredSpace(String spaceKey) {
        ao.executeInTransaction((TransactionCallback<Void>) {
            ao.find(IgnoredSpace.class, "IGNORED_SPACE_KEY = ?", spaceKey).each {
                try {
                    it.getEntityManager().delete(it)
                } catch (SQLException e) {
                    log.error(e.getMessage(), e)
                }
            }
            null
        })
    }

    @Override
    void setMonitoredGroup(String group) {
        ao.executeInTransaction({
            final MonitoredGroup monitoredGroup = ao.create(MonitoredGroup.class)
            monitoredGroup.setMonitoredGroup(group)
            monitoredGroup.save()
            monitoredGroup
        })
    }

    @Override
    Set<String> getMonitoredGroups() {
        def groups = new HashSet<>()
        ao.executeInTransaction((TransactionCallback<Void>) {
            ao.find(MonitoredGroup.class).each {groups << it.monitoredGroup}
            null
        })
        groups
    }

    @Override
    void removeMonitoredGroup(String group) {
        ao.executeInTransaction((TransactionCallback<Void>){
            ao.find(MonitoredGroup.class, "MONITORED_GROUP = ?", group).each {
                try {
                    it.getEntityManager().delete(it)
                } catch (SQLException e) {
                    log.error(e.getMessage(), e)
                }
            }
            null
        })
    }
}