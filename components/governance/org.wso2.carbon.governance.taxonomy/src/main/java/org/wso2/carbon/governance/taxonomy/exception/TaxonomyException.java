package org.wso2.carbon.governance.taxonomy.exception;

import org.wso2.carbon.governance.api.exception.GovernanceException;
/**
 * Base Class for capturing any type of exception that occurs when using the Taxonomy OSGi APIs.
 */
public class TaxonomyException  extends GovernanceException{

        /**
         * Constructs a new exception.
         */
        public TaxonomyException() {
            this("An unexpected error occurred.");
        }

        /**
         * Constructs a new exception with the specified detail message.
         *
         * @param message the detail message.
         */
        public TaxonomyException(String message) {
            super(message);
        }

        /**
         * Constructs a new exception with the specified detail message and cause.
         *
         * @param message the detail message.
         * @param cause   the cause of this exception.
         */
        public TaxonomyException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new exception with the specified cause.
         *
         * @param cause the cause of this exception.
         */
        public TaxonomyException(Throwable cause) {
            this("An unexpected error occurred.", cause);
        }

}
