package org.wso2.carbon.governance.gadgets.resourceimpact.util;

import org.wso2.carbon.governance.gadgets.resourceimpact.beans.AssociationBean;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

    public static AssociationBean[] getAssociations(String path, Registry registry,
                                                    boolean isDependency) throws RegistryException {
        List<AssociationBean> associationBeanList = new ArrayList<AssociationBean>();

        Association[] associations = registry.getAllAssociations(path);

        for (Association association : associations) {
            if ((!isDependency && path.equals(association.getSourcePath()) &&
                    !association.getDestinationPath().contains(";version:")) ||
                    (isDependency && association.getAssociationType().equals("depends") &&
                            path.equals(association.getDestinationPath()) &&
                            !association.getSourcePath().contains(";version:"))) {
                AssociationBean bean = new AssociationBean();
                bean.setAssociationType(association.getAssociationType());
                bean.setDestinationPath(association.getDestinationPath());
                bean.setSourcePath(association.getSourcePath());

                associationBeanList.add(bean);
            }
        }

        return associationBeanList.toArray(new AssociationBean[associationBeanList.size()]);
    }
}
