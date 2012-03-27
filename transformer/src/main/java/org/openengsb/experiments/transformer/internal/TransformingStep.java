package org.openengsb.experiments.transformer.internal;

public class TransformingStep {
    private String targetField;
    private TransformOperation operation;
    private String operationParam;
    private String[] sourceFields;

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public TransformOperation getOperation() {
        return operation;
    }

    public void setOperation(TransformOperation operation) {
        this.operation = operation;
    }

    public String getOperationParam() {
        return operationParam;
    }

    public void setOperationParam(String operationParam) {
        this.operationParam = operationParam;
    }

    public String[] getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(String... sourceFields) {
        this.sourceFields = sourceFields;
    }
}
