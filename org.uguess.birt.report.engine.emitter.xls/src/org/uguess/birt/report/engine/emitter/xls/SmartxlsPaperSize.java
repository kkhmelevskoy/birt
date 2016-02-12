package org.uguess.birt.report.engine.emitter.xls;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.birt.report.engine.content.IPageContent;
import org.eclipse.birt.report.engine.ir.DimensionType;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;


public class SmartxlsPaperSize
{
    public static short kPaperLetter = 1;
    public static short kPaperLetterSmall = 2;
    public static short kPaperTabloid = 3;
    public static short kPaperLedger = 4;
    public static short kPaperLegal = 5;
    public static short kPaperStatement = 6;
    public static short kPaperExecutive = 7;
    public static short kPaperA3 = 8;
    public static short kPaperA4 = 9;
    public static short kPaperA4Small = 10;
    public static short kPaperA5 = 11;
    public static short kPaperB4 = 12;
    public static short kPaperB5 = 13;
    public static short kPaperFolio = 14;
    public static short kPaperQuarto = 15;
    public static short kPaper10x14 = 16;
    public static short kPaper11x17 = 17;
    public static short kPaperNote = 18;
    public static short kPaperEnv9 = 19;
    public static short kPaperEnv10 = 20;
    public static short kPaperEnv11 = 21;
    public static short kPaperEnv12 = 22;
    public static short kPaperEnv14 = 23;
    public static short kPaperCSheet = 24;
    public static short kPaperDSheet = 25;
    public static short kPaperESheet = 26;
    public static short kPaperEnvDL = 27;
    public static short kPaperEnvC5 = 28;
    public static short kPaperEnvC3 = 29;
    public static short kPaperEnvC4 = 30;
    public static short kPaperEnvC6 = 31;
    public static short kPaperEnvC65 = 32;
    public static short kPaperEnvB4 = 33;
    public static short kPaperEnvB5 = 34;
    public static short kPaperEnvB6 = 35;
    public static short kPaperEnvItaly = 36;
    public static short kPaperEnvMonarch = 37;
    public static short kPaperEnvPersonal = 38;
    public static short kPaperFanfoldUS = 39;
    public static short kPaperFanfoldStdGerman = 40;
    public static short kPaperFanfoldLglGerman = 41;

    private static Map<String, Short> paperSizeMap = new HashMap<String, Short>();
    {
        paperSizeMap.put(DesignChoiceConstants.PAGE_SIZE_US_LEGAL, kPaperLegal);
        paperSizeMap.put(DesignChoiceConstants.PAGE_SIZE_A4, kPaperA4);
        paperSizeMap.put(DesignChoiceConstants.PAGE_SIZE_US_LETTER,
            kPaperLetter);
    }

    public static short paperSize(IPageContent content, short defaultPaperSize)
    {
        String birtPageType = content.getPageType();
        DimensionType width = content.getPageWidth();
        DimensionType height = content.getPageHeight();
        boolean landscape = DesignChoiceConstants.PAGE_ORIENTATION_LANDSCAPE
            .equals(content.getOrientation());

        Short paperSize = paperSizeMap.get(birtPageType);
        if (paperSize == null
            && DesignChoiceConstants.PAGE_SIZE_CUSTOM.equals(birtPageType))
        {
            DimensionType normWidth = width;
            DimensionType normHeight = height;
            if (landscape)
            {
                normWidth = height;
                normHeight = width;
            }

            if ((int) normWidth.convertTo(DesignChoiceConstants.UNITS_MM) == 297
                && (int) normHeight.convertTo(DesignChoiceConstants.UNITS_MM) == 420)
            {
                paperSize = kPaperA3;
            }
        }

        if (paperSize == null)
        {
            paperSize = defaultPaperSize;
        }

        return paperSize;
    }
}
