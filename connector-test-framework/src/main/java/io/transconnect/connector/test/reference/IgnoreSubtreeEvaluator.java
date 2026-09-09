/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;

/**
 * XMLUnit {@link DifferenceEvaluator} that skips the comparison of a whole element subtree when the
 * element carries the {@code ignore} attribute of the test namespace. Reference test cases use it for
 * elements with dynamic content, such as timestamps or generated IDs.
 */
public class IgnoreSubtreeEvaluator implements DifferenceEvaluator {

    /**
     * test namespace.
     */
    private final String testNamespace;

    /**
     * ignore namespace.
     */
    private final boolean ignoreNamespace;

    /**
     * constructs an evaluator which ignores subtrees if the ignore attribute is given.
     * @param testNamespace test namespace
     * @param ignore true, if    namespaces should be ignored while evaluation, otherwise false
     */
    IgnoreSubtreeEvaluator(String testNamespace, boolean ignore) {
        this.testNamespace = testNamespace;
        ignoreNamespace = ignore;
    }

    /**
     * evaluate the ignore status of nodes.
     * @param comparison Comparison object
     * @param comparisonResult the comparison result
     * @return comparison result
     */
    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult comparisonResult) {

        // disable check for child length check (allow additional nodes which can be ignored by using
        // test:ignore="true")
        if (comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH) {
            return ComparisonResult.EQUAL;
        }

        // only ignore namespaces if it is set
        if (comparison.getType() == ComparisonType.NAMESPACE_URI && ignoreNamespace) {
            return ComparisonResult.EQUAL;
        }

        // ignore the schema location
        if (comparison.getType() == ComparisonType.SCHEMA_LOCATION
                || comparison.getType() == ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION) {
            return ComparisonResult.EQUAL;
        }

        Node controlTarget = comparison.getControlDetails().getTarget();
        Node testTarget = comparison.getTestDetails().getTarget();

        if (hasIgnoreAncestor(controlTarget) || hasIgnoreAncestor(testTarget)) {
            return ComparisonResult.EQUAL;
        }

        return comparisonResult;
    }

    /**
     * checks the ancestors for ignore attribute to disable checks for the complete subtree.
     * @param node actual tested node
     * @return true, if it will be ignored, otherwise false
     */
    private boolean hasIgnoreAncestor(Node node) {
        if (node == null) {
            return false;
        }
        Node currentNode = node;
        if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
            currentNode = currentNode.getParentNode();
        }

        while (currentNode != null && currentNode.getNodeType() == Node.ELEMENT_NODE) {
            Element el = (Element) currentNode;
            if ("true".equals(el.getAttributeNS(testNamespace, "ignore"))) {
                return true;
            }
            currentNode = currentNode.getParentNode();
        }
        return false;
    }
}
