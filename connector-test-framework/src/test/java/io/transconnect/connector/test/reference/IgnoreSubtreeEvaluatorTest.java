/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Comparison.Detail;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;

class IgnoreSubtreeEvaluatorTest {

    private static final String TEST_NS = "http://test.io";

    /**
     * evaluation for the node list length is always equal.
     * needed if you have more nodes with ignores
     */
    @Test
    void testChildNodeListLengthAlwaysEqual() {
        DifferenceEvaluator ev = new IgnoreSubtreeEvaluator(TEST_NS, false);

        Comparison comparison = comparison(ComparisonType.CHILD_NODELIST_LENGTH, null, null);

        assertEquals(ComparisonResult.EQUAL, ev.evaluate(comparison, ComparisonResult.DIFFERENT));
        assertEquals(ComparisonResult.EQUAL, ev.evaluate(comparison, ComparisonResult.SIMILAR));
    }

    /**
     * no comparison of schema locations.
     */
    @Test
    void testSchemaLocationsAlwaysEqual() {
        DifferenceEvaluator ev = new IgnoreSubtreeEvaluator(TEST_NS, false);

        assertEquals(
                ComparisonResult.EQUAL,
                ev.evaluate(comparison(ComparisonType.SCHEMA_LOCATION, null, null), ComparisonResult.DIFFERENT));
        assertEquals(
                ComparisonResult.EQUAL,
                ev.evaluate(
                        comparison(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION, null, null),
                        ComparisonResult.DIFFERENT));
    }

    /**
     * tests ignoring/ no ignore of the namespace
     */
    @Test
    void testNamespaceUriEqualOnlyWhenIgnoreNamespaceTrue() {
        DifferenceEvaluator evFalse = new IgnoreSubtreeEvaluator(TEST_NS, false);
        DifferenceEvaluator evTrue = new IgnoreSubtreeEvaluator(TEST_NS, true);

        Comparison comparison = comparison(ComparisonType.NAMESPACE_URI, null, null);

        assertEquals(ComparisonResult.DIFFERENT, evFalse.evaluate(comparison, ComparisonResult.DIFFERENT));
        assertEquals(ComparisonResult.EQUAL, evTrue.evaluate(comparison, ComparisonResult.DIFFERENT));
    }

    /**
     * tests the return of the original document
     * @throws Exception when the doc creation fails
     */
    @Test
    void testNothingIsIgnored() throws Exception {
        DifferenceEvaluator ev = new IgnoreSubtreeEvaluator(TEST_NS, false);

        Document document = newDoc();
        Element root = document.createElementNS("http://test.any.io", "message");
        document.appendChild(root);

        Comparison comparison = comparison(ComparisonType.TEXT_VALUE, root, root);

        assertEquals(ComparisonResult.DIFFERENT, ev.evaluate(comparison, ComparisonResult.DIFFERENT));
        assertEquals(ComparisonResult.SIMILAR, ev.evaluate(comparison, ComparisonResult.SIMILAR));
    }

    @Test
    void ignoresSubtreeWhenControlAncestorHasIgnoreAttribute() throws Exception {
        DifferenceEvaluator ev = new IgnoreSubtreeEvaluator(TEST_NS, false);

        Document document = newDoc();

        Element message = document.createElementNS("http://test.any.io", "message");

        Element timestamp = document.createElementNS("http://test.any.io", "timestamp");
        timestamp.setAttributeNS(TEST_NS, "test:ignore", "true");

        Element child = document.createElementNS("http://test.any.io", "lastrun");
        Text text = document.createTextNode("4");

        document.appendChild(message);
        message.appendChild(timestamp);
        timestamp.appendChild(child);
        child.appendChild(text);

        // simulate a comparison on the text node (non-element target)
        Comparison comparison = comparison(ComparisonType.TEXT_VALUE, text, text);
        assertEquals(ComparisonResult.EQUAL, ev.evaluate(comparison, ComparisonResult.DIFFERENT));
    }

    /**
     * checks the deep ignore of ignore nodes.
     * @throws Exception if document creation fails
     */
    @Test
    void testIgnoresSubtreeWhenTestAncestorHasIgnoreAttribute() throws Exception {
        DifferenceEvaluator ev = new IgnoreSubtreeEvaluator(TEST_NS, false);

        Document document1 = newDoc();

        Element timestamp = document1.createElementNS("http://test.any.io", "timestamp");
        timestamp.setAttributeNS(TEST_NS, "test:ignore", "true");

        Element child = document1.createElementNS("http://test.any.io", "lastrun");
        Text text = document1.createTextNode("x");

        document1.appendChild(timestamp);
        timestamp.appendChild(child);
        child.appendChild(text);

        Document document2 = newDoc();

        Element timestamp2 = document2.createElementNS("http://test.any.io", "timestamp");
        timestamp2.setAttributeNS(TEST_NS, "test:ignore", "true");

        Element child2 = document2.createElementNS("http://test.any.io", "lastrun");
        Text text2 = document2.createTextNode("y");

        document2.appendChild(timestamp2);
        timestamp2.appendChild(child2);
        child2.appendChild(text2);

        // simulate a comparison on the attribute node (non-element target)
        Comparison comparison = comparison(ComparisonType.ATTR_VALUE, text, text2);

        assertEquals(ComparisonResult.EQUAL, ev.evaluate(comparison, ComparisonResult.DIFFERENT));
    }

    /**
     * checks if there are differences without test ignore.
     * @throws Exception if the documents could not be created
     */
    @Test
    void testNoIgnoresSubtreeWhenTestAncestorHasIgnoreAttribute() throws Exception {
        DifferenceEvaluator ev = new IgnoreSubtreeEvaluator(TEST_NS, false);

        Document document1 = newDoc();

        Element timestamp = document1.createElementNS("http://test.any.io", "timestamp");
        timestamp.setAttributeNS(TEST_NS, "test:ignore", "false");

        Element child = document1.createElementNS("http://test.any.io", "lastrun");
        Text text = document1.createTextNode("x");

        document1.appendChild(timestamp);
        timestamp.appendChild(child);
        child.appendChild(text);

        Document document2 = newDoc();

        Element timestamp2 = document2.createElementNS("http://test.any.io", "timestamp");

        Element child2 = document2.createElementNS("http://test.any.io", "lastrun");
        Text text2 = document2.createTextNode("y");

        document2.appendChild(timestamp2);
        timestamp2.appendChild(child2);
        child2.appendChild(text2);

        // simulate a comparison on the attribute node (non-element target)
        Comparison comparison = comparison(ComparisonType.ATTR_VALUE, text, text2);

        assertEquals(ComparisonResult.DIFFERENT, ev.evaluate(comparison, ComparisonResult.DIFFERENT));
    }

    /**
     * checks if only test:ignore ignores elements.
     * @throws Exception if document creation fails
     */
    @Test
    void testDoesNotIgnoreWhenIgnoreAttributeIsInDifferentNamespace() throws Exception {
        DifferenceEvaluator ev = new IgnoreSubtreeEvaluator(TEST_NS, false);

        Document document = newDoc();

        Element timestamp = document.createElementNS("http://test.any.io", "timestamp");
        // wrong namespace URI -> evaluator must not ignore
        timestamp.setAttributeNS("http://test.other.io", "other:ignore", "true");

        Element child = document.createElementNS("http://test.any.io", "lastrun");
        Text text = document.createTextNode("4");

        document.appendChild(timestamp);
        timestamp.appendChild(child);
        child.appendChild(text);

        Comparison comparison = comparison(ComparisonType.TEXT_VALUE, text, text);

        assertEquals(ComparisonResult.DIFFERENT, ev.evaluate(comparison, ComparisonResult.DIFFERENT));
    }

    /**
     * tests, if the nodes are null.
     */
    @Test
    void testNullTargetsDoNotTriggerIgnore() {
        DifferenceEvaluator ev = new IgnoreSubtreeEvaluator(TEST_NS, false);
        Comparison comparison = comparison(ComparisonType.TEXT_VALUE, null, null);
        assertEquals(ComparisonResult.DIFFERENT, ev.evaluate(comparison, ComparisonResult.DIFFERENT));
    }

    // ---------- helpers ----------
    private static Document newDoc() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().newDocument();
    }

    private static Comparison comparison(ComparisonType type, Node controlTarget, Node testTarget) {
        Comparison comparison = mock(Comparison.class);
        when(comparison.getType()).thenReturn(type);

        Detail control = mock(Detail.class);
        Detail test = mock(Detail.class);

        when(control.getTarget()).thenReturn(controlTarget);
        when(test.getTarget()).thenReturn(testTarget);

        when(comparison.getControlDetails()).thenReturn(control);
        when(comparison.getTestDetails()).thenReturn(test);

        return comparison;
    }
}
