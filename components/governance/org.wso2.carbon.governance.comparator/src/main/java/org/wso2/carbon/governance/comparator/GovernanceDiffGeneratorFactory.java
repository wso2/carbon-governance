package org.wso2.carbon.governance.comparator;

import org.wso2.carbon.governance.comparator.text.TextComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLComparator;

import java.util.ArrayList;
import java.util.List;

public class GovernanceDiffGeneratorFactory implements DiffGeneratorFactory {

    /*
    This will load DiffGenerator configuration from governance.xml file.
     */
    @Override
    public DiffGenerator getDiffGenerator() {
        List<Comparator<?>> comparators = new ArrayList<>();
        comparators.add(new TextComparator());
        comparators.add(new WSDLComparator());
        return new DiffGenerator(comparators);
    }
}
