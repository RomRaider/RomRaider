package enginuity.logger.definition;

import static enginuity.util.JEPUtil.evaluate;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.text.DecimalFormat;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.Map;

public final class EcuDerivedParameterConvertorImpl implements EcuDerivedParameterConvertor {
    private EcuData[] ecuDatas;
    private final String units;
    private final String expression;
    private final DecimalFormat format;
    private final Map<String, ExpressionInfo> expressionInfoMap = synchronizedMap(new HashMap<String, ExpressionInfo>());

    public EcuDerivedParameterConvertorImpl(String units, String expression, String format) {
        checkNotNullOrEmpty(units, "units");
        checkNotNullOrEmpty(expression, "expression");
        checkNotNullOrEmpty(format, "format");
        this.units = units;
        this.expression = expression;
        this.format = new DecimalFormat(format);
    }

    public double convert(byte[] bytes) {
        Map<String, Double> valueMap = new HashMap<String, Double>();
        int index = 0;
        for (EcuData ecuData : ecuDatas) {
            int length = ecuData.getAddresses().length;
            byte[] tmp = new byte[length];
            System.arraycopy(bytes, index, tmp, 0, length);
            ExpressionInfo expressionInfo = expressionInfoMap.get(ecuData.getId());
            valueMap.put(expressionInfo.getReplacementKey(), expressionInfo.getConvertor().convert(tmp));
            index += length;
        }
        double result = evaluate(expression, valueMap);
        return Double.isNaN(result) || Double.isInfinite(result) ? 0.0 : result;
    }

    public String getUnits() {
        return units;
    }

    public String format(double value) {
        return format.format(value);
    }

    public void setEcuDatas(EcuData[] ecuDatas) {
        checkNotNullOrEmpty(ecuDatas, "ecuDatas");
        this.ecuDatas = ecuDatas;
        for (EcuData ecuData : ecuDatas) {
            addExpressionInfo(ecuData);
        }
    }

    public String toString() {
        return getUnits();
    }

    private void addExpressionInfo(EcuData ecuData) {
        String id = ecuData.getId();
        String lookup = '[' + id + ':';
        int i = expression.indexOf(lookup);
        if (i >= 0) {
            int start = i + lookup.length();
            int end = expression.indexOf("]", start);
            String units = expression.substring(start, end);
            EcuDataConvertor selectedConvertor = null;
            EcuDataConvertor[] convertors = ecuData.getConvertors();
            for (EcuDataConvertor convertor : convertors) {
                if (units.equals(convertor.getUnits())) {
                    selectedConvertor = convertor;
                }
            }
            expressionInfoMap.put(id, new ExpressionInfo(id, selectedConvertor, true));
        } else {
            expressionInfoMap.put(id, new ExpressionInfo(id, ecuData.getSelectedConvertor(), false));
        }
    }

    private static final class ExpressionInfo {
        private final String ecuDataId;
        private final EcuDataConvertor convertor;
        private final String replacementKey;

        public ExpressionInfo(String ecuDataId, EcuDataConvertor convertor, boolean compositeKey) {
            checkNotNull(ecuDataId, convertor);
            this.ecuDataId = ecuDataId;
            this.convertor = convertor;
            this.replacementKey = compositeKey ? buildCompositeKey(ecuDataId, convertor.getUnits()) : ecuDataId;
        }

        public String getEcuDataId() {
            return ecuDataId;
        }

        public String getReplacementKey() {
            return replacementKey;
        }

        public EcuDataConvertor getConvertor() {
            return convertor;
        }

        private String buildCompositeKey(String ecuDataId, String convertorUnits) {
            if (convertorUnits == null || convertorUnits.length() == 0) {
                return ecuDataId;
            } else {
                return '[' + ecuDataId + ':' + convertorUnits + ']';
            }
        }
    }
}
