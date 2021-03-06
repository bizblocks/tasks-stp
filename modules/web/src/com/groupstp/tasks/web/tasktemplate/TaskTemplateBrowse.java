package com.groupstp.tasks.web.tasktemplate;

import com.groupstp.tasks.entity.TaskTemplate;
import com.groupstp.tasks.entity.TaskTypical;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.web.gui.components.WebTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

import static com.groupstp.tasks.web.tasktypical.TaskTypicalBrowse.MULTIPLE_SELECT_ON;

public class TaskTemplateBrowse extends EntityCombinedScreen {

    private static final Logger log = LoggerFactory.getLogger(TaskTemplateBrowse.class);

    @Inject
    private Table<TaskTemplate> tableOfTemplates;

    @Inject
    private Table<TaskTypical> taskTypicalTable;

    @Inject
    private DataManager dataManager;

    private TaskTemplate currentTemplate;

    @Override
    public void init(Map<String, Object> params) {

        // action on select template
        Action selectTaskAction = new BaseAction("") {
            @Override
            public void actionPerform(Component component) {
                Collection list = ((WebTable) component).getSelected();
                if (list.size() == 0) {
                    return;
                }
                TaskTemplate taskTemplate = (TaskTemplate) list.iterator().next();
                selectActiveTemplate(taskTemplate)
                ;
            }
        };
        tableOfTemplates.setItemClickAction(selectTaskAction);
        tableOfTemplates.setEnterPressAction(selectTaskAction);

        selectActiveTemplate(null);

        WindowParams.DISABLE_AUTO_REFRESH.set(params, true);

    }

    /**
     * Change active workspace from left(template's table) to right(typical tasks) and back
     *
     * @param taskTemplate - selected template ( null - if no selected template)
     */
    private void selectActiveTemplate(TaskTemplate taskTemplate) {
        boolean flag = Objects.nonNull(taskTemplate);

        currentTemplate = taskTemplate;

        getComponent("lookupBox").setEnabled(!flag);
        getComponent("editBox").setEnabled(flag);


        taskTypicalTable.getDatasource().refresh();
        taskTypicalTable.getDatasource().clear();

        if (currentTemplate != null) {
            loadTemplate();
        } else {
            tableOfTemplates.getDatasource().refresh();
        }

    }

    /**
     * Load typical tasks from current selected template to typical tasks table
     * In other words load current template
     */
    private void loadTemplate() {
        currentTemplate.getTasks().forEach(taskTypical ->
                taskTypicalTable.getDatasource().addItem(taskTypical)
        );

    }

    /**
     * Action on ADD button
     * Add typical task(s) to table
     *
     * @param source - don't used
     */
    public void onAddButton(Component source) {
        openLookup(TaskTypical.class, items -> {
            items.forEach(entity -> {
                taskTypicalTable.getDatasource().addItem((TaskTypical) entity);
            });
        }, WindowManager.OpenType.DIALOG, ParamsMap.of(MULTIPLE_SELECT_ON, ""));
    }

    /**
     * Action on DELETE button
     * Remove current typical task from table
     *
     * @param source - don't used
     */
    public void onDeleteButton(Component source) {
        Entity selectedLine = (Entity) ((WebTable) getComponent("taskTypicalTable")).getSelected().stream().findFirst().orElse(null);
        if (selectedLine != null) {
            taskTypicalTable.getDatasource().removeItem(selectedLine);
        }
    }

    /**
     * Action on CLEAR button
     * Clear typical tasks table
     *
     * @param source - don't used
     */
    public void onClearButton(Component source) {
        taskTypicalTable.getDatasource().clear();
    }

    /**
     * Save current selected typical tasks to current selected template
     * Change to left work space
     *
     * @param source - don't used
     */
    public void saveTemplate(Component source) {
        List<TaskTypical> items = new ArrayList(taskTypicalTable.getDatasource().getItems());
        currentTemplate.setTasks(items);
        dataManager.commit(currentTemplate);
        log.info("Template {} was saved", currentTemplate.getName());
        selectActiveTemplate(null);
    }

    /**
     * Clear typical tasks table
     * Change to left work space
     *
     * @param source - don't used
     */
    public void cancelTemplate(Component source) {
        selectActiveTemplate(null);
    }
}