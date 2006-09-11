package enginuity.logger.definition;

import static enginuity.util.JEPUtil.evaluate;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public final class EcuDerivedParameterConvertorImpl implements EcuDerivedParameterConvertor {
    private EcuData[] ecuDatas;
    private final String units;
    private final String expression;
    private final DecimalFormat format;

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
            valueMap.put(ecuData.getId(), ecuData.getSelectedConvertor().convert(tmp));
            index += length;
        }
        System.out.println("valueMap = " + valueMap);
        System.out.println("expression = " + expression);
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
    }
}
