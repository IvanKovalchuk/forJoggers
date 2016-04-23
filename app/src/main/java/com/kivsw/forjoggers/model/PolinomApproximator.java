package com.kivsw.forjoggers.model;

import com.kivsw.forjoggers.model.iApproximator;

/**
 * Created by ivan on 08.12.15.
 */
public class PolinomApproximator implements iApproximator {
    final int MAX_POLYNOM_POWER=100;
    private int FNr;
    private int FPowPol;//
    private int LnSys;//   -
    private int NumStep,NumMain;
    private double FX[],FY[], FA[],
                  B_[],C_[],D_[];
    private double A_[][];
    private int  IndX[];

    PolinomApproximator()
    {
        FX=null;
        FY=null;
        FNr=2;
        FA=null;
        FPowPol=0;
    }
    PolinomApproximator(int aPolPow)
    {
        FX=null;
        FY=null;
        FNr=2;
        FA=null;
        setPolynomPow(aPolPow);
    }
    void   setPolynomPow(int n)
    {
        if (n<1)
            n=1;

        if (n>MAX_POLYNOM_POWER)
           n=MAX_POLYNOM_POWER;

        FPowPol=n;
        LnSys=n+1;
        FA=new double[LnSys];

    };
    //---------------------------------------------------------------------------
    public double GetA(int i)
    {
        if (i<0 || i>FPowPol)
            return 0;
        return FA[i];
    };
    public String toString()//
    {
        StringBuilder S=new StringBuilder();
        if(FPowPol>1)
            for(int i=FPowPol;i>1;i--)
            {
                S.append(Double.toString(FA[i])+"*X^"+Integer.toString(i));
                if (FA[i-1]>=0) S.append("+");
            }

        S.append(Double.toString(FA[1])+"*X");
        if (FA[0]>=0) S.append("+");
        S.append(Double.toString(FA[0]));
        return S.toString();
    };
    public boolean approximate(double aX[], double aY[])
    {
        FNr= Math.min(aX.length,aY.length);
        FX=aX; FY=aY;

        return doApproximate();
    }

    public double function(double x)
    {
        double Sum=0;
        for(int i=FPowPol;i>=0;i--)
            Sum=Sum*x+FA[i];
        return Sum;
    }
    public double differential(double x)
    {
        double Sum=0;
        for(int i=FPowPol;i>=1;i--)
            Sum=Sum*x+FA[i]*i;
        return Sum;
    };
    //---------------------------------------------------------------------------
// II
    public double dDifferential(double x)
    {
        double Sum=0;
        for(int i=FPowPol;i>=2;i--)
            Sum=Sum*x+FA[i]*i*(i-1);
        return Sum;

    };
    //---------------------------------------------------------------------------
    public double sigma()
    {
        double SKO=0;
        for(int i=0;i<FNr;i++)
        {
            SKO+= Math.pow((FY[i] - function(FX[i])), 2);
        };
        return Math.pow(SKO / FNr, 0.5);
    };

    private boolean doApproximate()
    {
        if (FPowPol>=FNr)
            return false;

        try {
            A_ = new double[LnSys][LnSys];
            B_ = new double[LnSys];
            C_ = new double[LnSys * 2 + 1];
            D_ = new double[LnSys];
            IndX = new int[LnSys];

            findCD();
            formMatrix();
            for (int i = 0; i < LnSys; i++)
                IndX[i] = i;
            changeMatrix();
            findX();
        }catch(Exception e)
        {
            return false;
        }
        return true;

    }

    private void findCD()
    {
            double PowerX;
            C_[0]=FNr;
            D_[0]=0;
            for(int i=0;i<FNr;i++)
                D_[0]+=FY[i];
            for (int i=0;i<FNr;i++)
            {
                PowerX=1;
                for(int j=1;j<=2*FPowPol;j++)
                {
                    PowerX*=FX[i];
                    C_[j]+=PowerX;
                    if(j<=FPowPol) D_[j]+=PowerX*FY[i];
                };
            };
    }

    private void formMatrix()
    {
        for(int i=0;i<LnSys;i++)
        {
            B_[i]=D_[i];
            for(int j=0;j<LnSys;j++)
                A_[i][j]=C_[i+j];
        };
    };

    private void changeMatrix() throws Exception
    {
        for(NumStep=0;NumStep<LnSys-1;NumStep++)
        {
            findMainElement();
            exchangeCol();
            changeStr();
        };
    };

    private void findMainElement() throws Exception
    {
        double MainEl=A_[NumStep][NumStep];
        NumMain=NumStep;
        for(int k=NumStep+1;k<LnSys;k++)
            if(Math.abs(A_[NumStep][k])>Math.abs(MainEl) )
            {
                MainEl=A_[NumStep][k];
                NumMain=k;
            };
        if(MainEl==0)
        {
            throw new Exception("Cannot find main element");
        }
    };
    private void exchangeCol()
    {
        if (NumMain==NumStep) return;
        int IndCur=IndX[NumStep];
        IndX[NumStep]=IndX[NumMain];
        IndX[NumMain]=IndCur;

        double ElCur;
        for(int k=0;k<LnSys;k++)
        {
            ElCur=A_[k][NumStep];
            A_[k][NumStep]=A_[k][NumMain];
            A_[k][NumMain]=ElCur;
        };
    };
    private void changeStr()
    {
        double K;

        for(int k=NumStep+1;k<LnSys;k++)
        {
            K=A_[k][NumStep];
            B_[k]-=B_[NumStep]*K/A_[NumStep][NumStep];
            for(int l=NumStep;l<LnSys;l++)
            {
                A_[k][l]-=(A_[NumStep][l])*K/(A_[NumStep][NumStep]);
            }
        };
    };

    private void findX() throws Exception
    {
        if(FPowPol>=FNr)
        {
            throw new Exception("PowPol > Nr");
        };
        FA[IndX[FPowPol]]=B_[FPowPol]/A_[FPowPol][FPowPol];
        for(int k=FPowPol;k>=0;k--)
        {
            FA[IndX[k]]=B_[k];

            for(int l=FPowPol;l>k;l--)
                FA[IndX[k]]=FA[IndX[k]]-FA[IndX[l]]*A_[k][l];
            FA[IndX[k]]/=A_[k][k];
        };
    };
}

