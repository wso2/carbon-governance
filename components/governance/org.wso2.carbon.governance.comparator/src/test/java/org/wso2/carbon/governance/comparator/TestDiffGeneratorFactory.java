package org.wso2.carbon.governance.comparator;

import org.wso2.carbon.governance.comparator.wsdl.WSDLComparator;

import java.util.ArrayList;
import java.util.List;

public class TestDiffGeneratorFactory implements DiffGeneratorFactory {


    @Override
    public DiffGenerator getDiffGenerator() {
        List<Comparator<?>> comparators = new ArrayList<>();
        comparators.add(new WSDLComparator());
        return new DiffGenerator(comparators);
    }
}
