package analysis.exercise1;

import analysis.CallGraph;
import analysis.CallGraphAlgorithm;
import java.util.*;
import javax.annotation.Nonnull;

import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClassMember;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.views.JavaView;

public class CHAAlgorithm extends CallGraphAlgorithm {

  @Nonnull
  @Override
  protected String getAlgorithm() {
    return "CHA";
  }

  @Override
  protected void populateCallGraph(@Nonnull JavaView view, @Nonnull CallGraph cg) {
    // Your implementation goes here, also feel free to add methods as needed
    // To get your entry points we prepared getEntryPoints(view) in the superclass for you

    // TODO: implement
    getEntryPoints(view).forEach(entryPoint -> {
      cg.addNode(entryPoint);

      view.getMethod(entryPoint).ifPresent(method -> {
        if (method.hasBody()) {
          for (Stmt stmt : method.getBody().getStmtGraph().getStmts()) {
            if (stmt instanceof JInvokeStmt) {
              JInvokeStmt invokeStmt = (JInvokeStmt) stmt;
              AbstractInvokeExpr invokeExpr = invokeStmt.getInvokeExpr();

              if (invokeExpr instanceof JSpecialInvokeExpr) {
                MethodSignature target = invokeExpr.getMethodSignature();
                addNodeAndEdge(cg, entryPoint, target);
              }

              if (invokeExpr instanceof JVirtualInvokeExpr) {
                JVirtualInvokeExpr virtualInvoke = (JVirtualInvokeExpr) invokeExpr;
                String methodName = virtualInvoke.getMethodSignature().getName();

                view.getClass((ClassType) virtualInvoke.getBase().getType()).ifPresent(clazz -> {
                  clazz.getMethods().stream()
                          .filter(m -> m.getName().equals(methodName))
                          .map(SootClassMember::getSignature)
                          .forEach(target -> addNodeAndEdge(cg, entryPoint, target));
                });
              }
            }
          }
        }
      });
    });
  }

  private void addNodeAndEdge(@Nonnull CallGraph cg, @Nonnull MethodSignature source, @Nonnull MethodSignature target) {
    if (!cg.hasNode(target)) {
      cg.addNode(target);
    }
    cg.addEdge(source, target);
  }
}
