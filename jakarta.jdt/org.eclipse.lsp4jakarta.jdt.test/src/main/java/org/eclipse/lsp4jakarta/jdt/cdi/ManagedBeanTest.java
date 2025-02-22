package org.eclipse.lsp4jakarta.jdt.cdi;

import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.*;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4jakarta.commons.JakartaDiagnosticsParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.jdt.core.BaseJakartaTest;
import org.eclipse.lsp4jakarta.jdt.core.JDTUtils;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

public class ManagedBeanTest extends BaseJakartaTest {

    protected static JDTUtils JDT_UTILS = new JDTUtils();

    @Test
    public void managedBeanAnnotations() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject()
                .getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ManagedBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test expected diagnostic
        Diagnostic d1 = d(6, 12, 13,
                "The annotation @Dependent must be the only scope defined by a managed bean with a non-static public field.",
                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidManagedBeanAnnotation");
        
        Diagnostic d2 = d(5, 13, 24,
                "Managed bean class of generic type must have scope @Dependent",
                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidManagedBeanAnnotation");

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d1, d2);

        // Assert for the diagnostic d1
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1 = te(4, 0, 5, 0, "@Dependent\n");
        CodeAction ca1 = ca(uri, "Replace current scope with @Dependent", d1, te1);
        assertJavaCodeAction(codeActionParams1, JDT_UTILS, ca1);
        
        // Assert for the diagnostic d2
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te2 = te(4, 0, 5, 0, "@Dependent\n");
        CodeAction ca2 = ca(uri, "Replace current scope with @Dependent", d2, te2);
        assertJavaCodeAction(codeActionParams2, JDT_UTILS, ca2);
    }
    
    @Test
    public void scopeDeclaration() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject()
                .getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ScopeDeclaration.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test expected diagnostic
        Diagnostic d1 = d(12, 16, 17,
                "Scope type annotations must be specified by a producer field at most once.",
                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidScopeDecl");
        d1.setData(new Gson().toJsonTree(Arrays.asList("ApplicationScoped", "Dependent", "Produces")));

        Diagnostic d2 = d(15, 25, 41, "Scope type annotations must be specified by a producer method at most once.",
                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidScopeDecl");
        d2.setData(new Gson().toJsonTree(Arrays.asList("ApplicationScoped", "RequestScoped", "Produces")));
        
        Diagnostic d3 = d(10, 13, 29, "Scope type annotations must be specified by a managed bean class at most once.",
                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidScopeDecl");
        d3.setData(new Gson().toJsonTree(Arrays.asList("ApplicationScoped", "RequestScoped")));

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d1, d2, d3);

        // Assert for the diagnostic d1
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1 = te(11, 33, 12, 4, "");
        TextEdit te2 = te(11, 14, 11, 33, "");
        CodeAction ca1 = ca(uri, "Remove @Dependent", d1, te1);
        CodeAction ca2 = ca(uri, "Remove @ApplicationScoped", d1, te2);
        
        assertJavaCodeAction(codeActionParams1, JDT_UTILS, ca1, ca2);
        
        // Assert for the diagnostic d2
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te3 = te(14, 33, 15, 4, "");
        TextEdit te4 = te(14, 14, 14, 33, "");
        CodeAction ca3 = ca(uri, "Remove @RequestScoped", d2, te3);
        CodeAction ca4 = ca(uri, "Remove @ApplicationScoped", d2, te4);
        
        assertJavaCodeAction(codeActionParams2, JDT_UTILS, ca3, ca4);
        
        // Assert for the diagnostic d3
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d3);
        TextEdit te5 = te(9, 19, 10, 0, "");
        TextEdit te6 = te(9, 0, 9, 19, "");
        CodeAction ca5 = ca(uri, "Remove @RequestScoped", d3, te5);
        CodeAction ca6 = ca(uri, "Remove @ApplicationScoped", d3, te6);
        
        assertJavaCodeAction(codeActionParams3, JDT_UTILS, ca5, ca6);
    }

    @Test
    public void producesAndInject() throws Exception {
        JDTUtils utils = JDT_UTILS;
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject()
                .getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ProducesAndInjectTogether.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(16, 18, 23, "The annotations @Produces and @Inject must not be used on the same field or property.",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrInject");

        Diagnostic d2 = d(11, 19, 27, "The annotations @Produces and @Inject must not be used on the same field or property.",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrInject");

        assertJavaDiagnostics(diagnosticsParams, utils, d1, d2);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);

        TextEdit te1 = te(14, 4, 15, 4, "");
        TextEdit te2 = te(15, 4, 16, 4, "");
        CodeAction ca1 = ca(uri, "Remove @Produces", d1, te1);
        CodeAction ca2 = ca(uri, "Remove @Inject", d1, te2);

        assertJavaCodeAction(codeActionParams1, utils, ca1, ca2);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);

        TextEdit te3 = te(9, 4, 10, 4, "");
        TextEdit te4 = te(10, 4, 11, 4, "");
        CodeAction ca3 = ca(uri, "Remove @Produces", d2, te3);
        CodeAction ca4 = ca(uri, "Remove @Inject", d2, te4);

        assertJavaCodeAction(codeActionParams2, utils, ca3, ca4);
    }

    @Test
    public void injectAndDisposesObservesObservesAsync() throws Exception {
        JDTUtils utils = JDT_UTILS;
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path(
                "src/main/java/io/openliberty/sample/jakarta/cdi/InjectAndDisposesObservesObservesAsync.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(10, 18, 31,
                "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveInjectOrConflictedAnnotations");

        Diagnostic d2 = d(16, 18, 31,
                "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Observes",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveInjectOrConflictedAnnotations");

        Diagnostic d3 = d(22, 18, 36,
                "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveInjectOrConflictedAnnotations");

        Diagnostic d4 = d(28, 18, 39,
                "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes, @Observes",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveInjectOrConflictedAnnotations");

        Diagnostic d5 = d(34, 18, 44,
                "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Observes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveInjectOrConflictedAnnotations");

        Diagnostic d6 = d(40, 18, 44,
                "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveInjectOrConflictedAnnotations");

        Diagnostic d7 = d(46, 18, 52,
                "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes, @Observes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveInjectOrConflictedAnnotations");
        
        Diagnostic d8 = d(51, 18, 53,
                "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes, @Observes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveInjectOrConflictedAnnotations");

        assertJavaDiagnostics(diagnosticsParams, utils, d1, d2, d3, d4, d5, d6, d7, d8);
        
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);

        TextEdit te1 = te(9, 4, 10, 4, "");
        TextEdit te2 = te(10, 32, 10, 42, "");
        CodeAction ca1 = ca(uri, "Remove @Inject", d1, te1);
        CodeAction ca2 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name'", d1, te2);

        assertJavaCodeAction(codeActionParams1, utils, ca1, ca2);
        
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);

        TextEdit te3 = te(15, 4, 16, 4, "");
        TextEdit te4 = te(16, 32, 16, 42, "");
        CodeAction ca3 = ca(uri, "Remove @Inject", d2, te3);
        CodeAction ca4 = ca(uri, "Remove the '@Observes' modifier from parameter 'name'", d2, te4);

        assertJavaCodeAction(codeActionParams2, utils, ca3, ca4);
        
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d3);

        TextEdit te5 = te(21, 4, 22, 4, "");
        TextEdit te6 = te(22, 37, 22, 52, "");
        CodeAction ca5 = ca(uri, "Remove @Inject", d3, te5);
        CodeAction ca6 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name'", d3, te6);

        assertJavaCodeAction(codeActionParams3, utils, ca5, ca6);
        
        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, d4);

        TextEdit te7 = te(27, 4, 28, 4, "");
        TextEdit te8 = te(28, 40, 28, 50, "");
        TextEdit te9 = te(28, 64, 28, 74, "");
        CodeAction ca7 = ca(uri, "Remove @Inject", d4, te7);
        CodeAction ca8 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", d4, te8);
        CodeAction ca9 = ca(uri, "Remove the '@Observes' modifier from parameter 'name2'", d4, te9);

        assertJavaCodeAction(codeActionParams4, utils, ca7, ca8, ca9);
        
        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, d5);

        TextEdit te10 = te(33, 4, 34, 4, "");
        TextEdit te11 = te(34, 45, 34, 55, "");
        TextEdit te12 = te(34, 69, 34, 84, "");
        CodeAction ca10 = ca(uri, "Remove @Inject", d5, te10);
        CodeAction ca11 = ca(uri, "Remove the '@Observes' modifier from parameter 'name1'", d5, te11);
        CodeAction ca12 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name2'", d5, te12);

        assertJavaCodeAction(codeActionParams5, utils, ca10, ca11, ca12);
        
        JakartaJavaCodeActionParams codeActionParams6 = createCodeActionParams(uri, d6);

        TextEdit te13 = te(39, 4, 40, 4, "");
        TextEdit te14 = te(40, 45, 40, 55, "");
        TextEdit te15 = te(40, 69, 40, 84, "");
        CodeAction ca13 = ca(uri, "Remove @Inject", d6, te13);
        CodeAction ca14 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", d6, te14);
        CodeAction ca15 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name2'", d6, te15);

        assertJavaCodeAction(codeActionParams6, utils, ca13, ca14, ca15);
        
        JakartaJavaCodeActionParams codeActionParams7 = createCodeActionParams(uri, d7);

        TextEdit te16 = te(45, 4, 46, 4, "");
        TextEdit te17 = te(46, 53, 46, 63, "");
        TextEdit te18 = te(46, 77, 46, 87, "");
        TextEdit te19 = te(46, 101, 46, 116, "");
        CodeAction ca16 = ca(uri, "Remove @Inject", d7, te16);
        CodeAction ca17 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", d7, te17);
        CodeAction ca18 = ca(uri, "Remove the '@Observes' modifier from parameter 'name2'", d7, te18);
        CodeAction ca19 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name3'", d7, te19);

        assertJavaCodeAction(codeActionParams7, utils, ca16, ca17, ca18, ca19);
        
        JakartaJavaCodeActionParams codeActionParams8 = createCodeActionParams(uri, d8);

        TextEdit te20 = te(50, 4, 51, 4, "");
        TextEdit te21 = te(51, 54, 51, 89, "");
        CodeAction ca20 = ca(uri, "Remove @Inject", d8, te20);
        CodeAction ca21 = ca(uri, "Remove the '@Disposes', '@Observes', '@ObservesAsync' modifier from parameter 'name'", d8, te21);

        assertJavaCodeAction(codeActionParams8, utils, ca20, ca21);

    }

    @Test
    public void producesAndDisposesObservesObservesAsync() throws Exception {
        JDTUtils utils = JDT_UTILS;
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path(
                "src/main/java/io/openliberty/sample/jakarta/cdi/ProducesAndDisposesObservesObservesAsync.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(12, 18, 31,
                "A producer method cannot have parameter(s) annotated with @Disposes",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrConflictedAnnotations");

        Diagnostic d2 = d(18, 18, 31,
                "A producer method cannot have parameter(s) annotated with @Observes",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrConflictedAnnotations");

        Diagnostic d3 = d(24, 18, 36,
                "A producer method cannot have parameter(s) annotated with @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrConflictedAnnotations");

        Diagnostic d4 = d(30, 18, 39,
                "A producer method cannot have parameter(s) annotated with @Disposes, @Observes",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrConflictedAnnotations");

        Diagnostic d5 = d(36, 18, 44,
                "A producer method cannot have parameter(s) annotated with @Observes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrConflictedAnnotations");

        Diagnostic d6 = d(42, 18, 44,
                "A producer method cannot have parameter(s) annotated with @Disposes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrConflictedAnnotations");

        Diagnostic d7 = d(48, 18, 52,
                "A producer method cannot have parameter(s) annotated with @Disposes, @Observes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrConflictedAnnotations");
        
        Diagnostic d8 = d(54, 18, 53,
                "A producer method cannot have parameter(s) annotated with @Disposes, @Observes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveProducesOrConflictedAnnotations");
        
        Diagnostic d9 = d(30, 18, 39,
                "A disposer method cannot have parameter(s) annotated with @Observes",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveDisposesOrConflictedAnnotations");
        
        Diagnostic d10 = d(42, 18, 44,
                "A disposer method cannot have parameter(s) annotated with @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveDisposesOrConflictedAnnotations");
        
        Diagnostic d11 = d(48, 18, 52,
                "A disposer method cannot have parameter(s) annotated with @Observes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveDisposesOrConflictedAnnotations");
        
        Diagnostic d12 = d(54, 18, 53,
                "A disposer method cannot have parameter(s) annotated with @Observes, @ObservesAsync",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveDisposesOrConflictedAnnotations");
        
        assertJavaDiagnostics(diagnosticsParams, utils, d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12);

        
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);

        TextEdit te1 = te(11, 4, 12, 4, "");
        TextEdit te2 = te(12, 32, 12, 42, "");
        CodeAction ca1 = ca(uri, "Remove @Produces", d1, te1);
        CodeAction ca2 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name'", d1, te2);

        assertJavaCodeAction(codeActionParams1, utils, ca1, ca2);
        
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);

        TextEdit te3 = te(17, 4, 18, 4, "");
        TextEdit te4 = te(18, 32, 18, 42, "");
        CodeAction ca3 = ca(uri, "Remove @Produces", d2, te3);
        CodeAction ca4 = ca(uri, "Remove the '@Observes' modifier from parameter 'name'", d2, te4);

        assertJavaCodeAction(codeActionParams2, utils, ca3, ca4);
        
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d3);

        TextEdit te5 = te(23, 4, 24, 4, "");
        TextEdit te6 = te(24, 37, 24, 52, "");
        CodeAction ca5 = ca(uri, "Remove @Produces", d3, te5);
        CodeAction ca6 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name'", d3, te6);

        assertJavaCodeAction(codeActionParams3, utils, ca5, ca6);
        
        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, d4);

        TextEdit te7 = te(29, 4, 30, 4, "");
        TextEdit te8 = te(30, 40, 30, 50, "");
        TextEdit te9 = te(30, 64, 30, 74, "");
        CodeAction ca7 = ca(uri, "Remove @Produces", d4, te7);
        CodeAction ca8 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", d4, te8);
        CodeAction ca9 = ca(uri, "Remove the '@Observes' modifier from parameter 'name2'", d4, te9);

        assertJavaCodeAction(codeActionParams4, utils, ca7, ca8, ca9);
        
        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, d5);

        TextEdit te10 = te(35, 4, 36, 4, "");
        TextEdit te11 = te(36, 45, 36, 55, "");
        TextEdit te12 = te(36, 69, 36, 84, "");
        CodeAction ca10 = ca(uri, "Remove @Produces", d5, te10);
        CodeAction ca11 = ca(uri, "Remove the '@Observes' modifier from parameter 'name1'", d5, te11);
        CodeAction ca12 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name2'", d5, te12);

        assertJavaCodeAction(codeActionParams5, utils, ca10, ca11, ca12);
        
        JakartaJavaCodeActionParams codeActionParams6 = createCodeActionParams(uri, d6);

        TextEdit te13 = te(41, 4, 42, 4, "");
        TextEdit te14 = te(42, 45, 42, 55, "");
        TextEdit te15 = te(42, 69, 42, 84, "");
        CodeAction ca13 = ca(uri, "Remove @Produces", d6, te13);
        CodeAction ca14 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", d6, te14);
        CodeAction ca15 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name2'", d6, te15);

        assertJavaCodeAction(codeActionParams6, utils, ca13, ca14, ca15);
        
        JakartaJavaCodeActionParams codeActionParams7 = createCodeActionParams(uri, d7);

        TextEdit te16 = te(47, 4, 48, 4, "");
        TextEdit te17 = te(48, 53, 48, 63, "");
        TextEdit te18 = te(48, 77, 48, 87, "");
        TextEdit te19 = te(48, 101, 48, 116, "");
        CodeAction ca16 = ca(uri, "Remove @Produces", d7, te16);
        CodeAction ca17 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", d7, te17);
        CodeAction ca18 = ca(uri, "Remove the '@Observes' modifier from parameter 'name2'", d7, te18);
        CodeAction ca19 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name3'", d7, te19);

        assertJavaCodeAction(codeActionParams7, utils, ca16, ca17, ca18, ca19);
        
        JakartaJavaCodeActionParams codeActionParams8 = createCodeActionParams(uri, d8);

        TextEdit te20 = te(53, 4, 54, 4, "");
        TextEdit te21 = te(54, 54, 54, 89, "");
        CodeAction ca20 = ca(uri, "Remove @Produces", d8, te20);
        CodeAction ca21 = ca(uri, "Remove the '@Disposes', '@Observes', '@ObservesAsync' modifier from parameter 'name'", d8, te21);

        assertJavaCodeAction(codeActionParams8, utils, ca20, ca21);
        
    }
    
    @Test
    public void multipleDisposes() throws Exception {
        JDTUtils utils = JDT_UTILS;
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject()
                .getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/MultipleDisposes.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();
        
        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        
        Diagnostic d = d(9, 18, 23,
                "The annotation @Disposes must not be defined on more than one parameter of a method.",
                DiagnosticSeverity.Error, "jakarta-cdi", "RemoveExtraDisposes");
        
        assertJavaDiagnostics(diagnosticsParams, utils, d);
    }
}
