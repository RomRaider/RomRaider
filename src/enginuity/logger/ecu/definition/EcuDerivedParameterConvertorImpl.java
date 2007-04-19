/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity.logger.ecu.definition;

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
    private final Map<String, String> replaceMap;
    private final Map<String, ExpressionInfo> expressionInfoMap = synchronizedMap(new HashMap<String, ExpressionInfo>());

    public EcuDerivedParameterConvertorImpl(String units, String expression, String format, Map<String, String> replaceMap) {
        checkNotNullOrEmpty(units, "units");
        checkNotNullOrEmpty(expression, "expression");
        checkNotNullOrEmpty(format, "format");
        checkNotNull(replaceMap, "replaceMap");
        this.units = units;
        this.expression = expression;
        this.format = new DecimalFormat(format);
        this.replaceMap = replaceMap;
    }

    public double convert(byte[] bytes) {
        Map<String, Double> valueMap = new HashMap<String, Double>();
        String exp = expression;
        int index = 0;
        for (EcuData ecuData : ecuDatas) {
            int length = ecuData.getAddress().getLength();
            byte[] tmp = new byte[length];
            System.arraycopy(bytes, index, tmp, 0, length);
            ExpressionInfo expressionInfo = expressionInfoMap.get(ecuData.getId());
            valueMap.put(expressionInfo.getReplacementKey(), expressionInfo.getConvertor().convert(tmp));
            exp = exp.replace(buildParameterKey(expressionInfo), expressionInfo.getReplacementKey());
            index += length;
        }
        double result = evaluate(exp, valueMap);
        return Double.isNaN(result) || Double.isInfinite(result) ? 0.0 : result;
    }

    public String getUnits() {
        return units;
    }

    public String format(double value) {
        String formattedValue = format.format(value);
        if (replaceMap.containsKey(formattedValue)) {
            return replaceMap.get(formattedValue);
        } else {
            return formattedValue;
        }
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

    private String buildParameterKey(ExpressionInfo expressionInfo) {
        return '[' + expressionInfo.getEcuDataId() + ':' + expressionInfo.getConvertor().getUnits() + ']';
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
                return '_' + ecuDataId + '_' + convertorUnits + '_';
            }
        }
    }
}
