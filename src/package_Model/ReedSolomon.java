/* Initialize a RS codec
 *
 * Copyright 2002 Phil Karn, KA9Q
 * May be used under the terms of the GNU General Public License (GPL)
 *	Reed-Solomon decoder
 *	Copyright 2002 Phil Karn, KA9Q
 *	May be used under the terms of the GNU General Public License (GPL)
 *
 *	Rewritten - and slightly adapted while doing so -
 *	as a java class for use in the javaDAB program
 *	Copyright 2017 Jan van Katwijk
 *	May be used under the terms of the GNU General Public License (GPL)
 */


package package_Model;
import java.util.Arrays;

/* Initialize a Reed-Solomon codec
 * symsize      = symbol size, bits (1-8)
 * gfpoly       = Field generator polynomial coefficients
 * fcr          = first root of RS code generator polynomial, index form, 0
 * prim         = primitive element to generate polynomial roots
 * nroots       = RS code generator polynomial degree (number of roots)
 */

public class   ReedSolomon  extends Galois {
        private final int	symsize;       /* Bits per symbol */
        private final int	codeLength;    /* Symbols per block (= (1<<mm)-1) */
        private final int []	generator;     /* Generator polynomial */
        private final int	nroots;   /* Number of generator roots = number of parity symbols */
        private 	int	fcr;   /* First consecutive root, index form */
        private		int	prim;  /* Primitive element, index form */
        private final	int	iprim; /* prim-th root of 1, index form */

	public ReedSolomon (int	symsize,
	                    int	gfpoly,
	                    int fcr,
	                    int	prim,
	                    int	nroots) {
	   super (symsize, gfpoly);
	   int root, iprim_1;

	   this. symsize	= symsize;              // in bits
	   this. codeLength	= (1 << symsize) - 1;
	   this. fcr		= fcr;
	   this. prim		= prim;
	   this. nroots		= nroots;

	   for (int i = 1; ; i += codeLength) {
	      iprim_1 = i;
	      if ((iprim_1 % prim) == 0)
	         break;
	   }
	   this. iprim = iprim_1 / prim;

	   generator	= new int [nroots + 1];
	   generator [0] = 1;
	   root		= fcr * prim;
	   for (int i = 0; i < nroots; i++, root += 1) {
	      generator [i + 1] = 1;
	      for (int j = i; j > 0; j--){
	         if (generator [j] != 0) {
	            int p1 = multiply_power (poly2power (generator [j]), root);
	            generator [j] = add_poly (generator [j - 1],
	                                              power2poly (p1));
	         }
	         else {
	            generator [j] = generator [j - 1];
	         }
	      }
//	Handle -> genpoly [0] can never be zero */
	      generator [0] =
	                power2poly (multiply_power (root,
	                            poly2power (generator [0])));
 	   }

	   for (int i = 0; i <= nroots; i ++)
	      generator [i] = poly2power (generator [i]);
	}

	public int dec (final byte [] r, byte [] d, int cutlen) {
	   int [] rf 	= new int [codeLength];
	
	   Arrays. fill (rf, 0);
	   for (int i = cutlen; i < codeLength; i++)
              rf [i] = (r [i - cutlen] & 0xFF);

	   int ret = decode_rs (rf);
	   for (int i = cutlen; i < codeLength - nroots; i++)
	      d [i - cutlen] = (byte)(rf [i] & 0xFF);
	   return ret;
	}

	private int decode_rs (int [] data) {
	   int []  syndromes	= new int [nroots + 1];
	   int []  Lambda	= new int [nroots + 1];
	   int []  rootTable 	= new int [nroots];
	   int []  locTable	= new int [nroots];
	   int []  omega	= new int [nroots + 1];
	   int lambda_degree, omega_degree;
	   int rootCount;
//
//      returning syndromes in poly
	   if (computeSyndromes (data, syndromes))
	      return 0;

//	Step 2: Berlekamp-Massey (Lambda in power notation)
	   lambda_degree = computeLambda (syndromes, Lambda);

//	Step 3: evaluate lambda and compute the error locations (chien)
	   rootCount = computeErrors (Lambda, lambda_degree,
	                              rootTable, locTable);
	   if (rootCount < 0)
	      return -1;

	   omega_degree = computeOmega (syndromes, Lambda,
	                                lambda_degree, omega);
/*
 *      Compute error values in poly-form.
 *      num1 = omega (inv (X (l))),
 *      num2 = inv (X (l))**(FCR-1) and
 *      den = lambda_pr(inv(X(l))) all in poly-form
 */
	   int num1, num2, den;
	   for (int j = rootCount - 1; j >= 0; j--) {
	      num1 = 0;
	      for (int i = omega_degree; i >= 0; i--) {
	         if (omega [i] != codeLength) {
	            int tmp = multiply_power (omega [i],
	                                      pow_power (i, rootTable [j]));
	            num1   = add_poly (num1, power2poly (tmp));
	         }
	      }

	      int tmp = multiply_power (pow_power (rootTable [j],
	                                           divide_power (fcr, 1)),
	                                codeLength);
	      num2 = power2poly (tmp);
	      den = 0;
/*
 *      lambda [i + 1] for i even is the formal derivative
 *      lambda_pr of lambda [i]
 */
	      for (int i = Math. min (lambda_degree, nroots - 1) & ~1;
                         i >= 0; i -=2) {
	         if (Lambda [i + 1] != codeLength) {
	            int tmp_1 = multiply_power (Lambda [i + 1],
	                                        pow_power (i, rootTable [j]));
	            den     = add_poly (den, power2poly (tmp_1));
	         }
	      }

	      if (den == 0) {
//	         System. out. println ("den = 0, (count was " + den + " )");
	         return -1;
	      }

//	Apply error to data */
	      if (num1 != 0) {
	         if (locTable [j] >=  codeLength - nroots)
	            rootCount --;
	         else {
	            int tmp1  = codeLength - poly2power (den);
	            int tmp2  = multiply_power (poly2power (num1),
	                                        poly2power (num2));
	            tmp2      = multiply_power (tmp2, tmp1);
	            int corr  = power2poly (tmp2);
	            data [locTable [j]] =
	                              add_poly (data [locTable [j]], corr);
	         }
	      }
	   }
	   return rootCount;
	}


//      Apply Horner on the input for root "root"
	private int getSyndrome (int [] data, int root) {
	   int syn     = data [0];

	   for (int j = 1; j < codeLength; j++) {
	      if (syn == 0)
	         syn = data [j];
	      else {
	         int uu1 = pow_power (multiply_power (fcr, root), prim);
	         syn = add_poly (data [j], power2poly (
	                                               multiply_power (
	                                               poly2power (syn), uu1)));
	      }
	   }
	   return syn;
	}

//      use Horner to compute the syndromes
	private boolean computeSyndromes (int [] data, int [] syndromes) {
	   int syn_error = 0;

/*	form the syndromes; i.e., evaluate data (x) at roots of g(x) */

	   for (int i = 0; i < nroots; i++) {
	      syndromes [i] = getSyndrome (data, i);
	      syn_error |= syndromes [i];
	   }

	   return syn_error == 0;
	}

//      compute Lambda with Berlekamp-Massey
//      syndromes in poly-form in, Lambda in power form out
//     
	private	int computeLambda (int [] syndromes, int [] Lambda) {
	   int K = 1, L = 0;
	   int [] Corrector	= new int [Lambda. length];
	   int [] oldLambda	= new int [Lambda. length];
	   int  error		= syndromes [0];
	   int	deg_Lambda	= 0;

	   Arrays. fill (Lambda, 0);
	   Arrays. fill (Corrector, 0);
//
//      Initializers:
	   Lambda  [0]     = 1;
	   Corrector [1]   = 1;
	   while (K <= nroots) {
	      System. arraycopy (Lambda, 0, oldLambda, 0, nroots);

//      Compute new lambda
	      for (int i = 0; i < nroots; i ++)
	         Lambda [i] = add_poly (Lambda [i],
                                        multiply_poly (error, Corrector [i]));
	      if ((2 * L < K) && (error != 0)) {
	         L = K - L;
	         for (int i = 0; i < nroots; i ++)
	             Corrector [i] = divide_poly (oldLambda [i], error);
	      }
//
//      multiply x * C (x), i.e. shift to the right, the 0-th order term is left
	      for (int i = nroots - 1; i >= 1; i --)
	         Corrector [i] = Corrector [i - 1];
	      Corrector [0] = 0;

//      and compute a new error
	      error        = syndromes [K];
	      for (int i = 1; i <= K; i ++)  {
	         error = add_poly (error, multiply_poly (syndromes [K - i],
	                                                 Lambda [i]));
	      }
	      K += 1;
	   } // end of Berlekamp loop

	   for (int i = 0; i < nroots; i ++) {
	      if (Lambda [i] != 0)
	         deg_Lambda = i;
	      Lambda [i] = poly2power (Lambda [i]);
	   }
	   return deg_Lambda;
	}
//
//      Compute the roots of lambda by evaluating the
//      lambda polynome for all (inverted) powers of the symbols
//      of the data (Chien search)
	private int computeErrors (int []	Lambda,
	                           int		deg_lambda,
	                           int []	rootTable,
	                           int []	locTable) {
	   int rootCount = 0;
	   int [] workRegister = new int [nroots + 1];
	   System. arraycopy (Lambda, 0, workRegister, 0, nroots + 1);
//
//      reg is lambda in power notation
	   int k = iprim - 1;
	   for (int i = 1; i <= codeLength; i ++, k = (k + iprim)) {
	      int result = 1; // lambda [0] is always 1
//      Note that for i + 1, the powers in the workregister just need
//      to be increased by "j".
	      for (int j = deg_lambda; j > 0; j --) {
	         if (workRegister [j] != codeLength)  {
	            workRegister [j] = multiply_power (workRegister [j], j);
	            result = add_poly (result, power2poly (workRegister [j]));
	         }
	      }
	      if (result == 0) {             // root
	         rootTable [rootCount] = i;
	         locTable  [rootCount] = k;
	         rootCount ++;
	      }
	   }

	   if (rootCount != deg_lambda)
	      return -1;
	   return rootCount;
	}

/*
 *      Compute error evaluator poly
 *      omega(x) = s(x)*lambda(x) (modulo x**NROOTS)
 *      in power form, and  find degree (omega).
 *
 *      Note that syndromes are in poly form, while lambda in power form
 */
	private int computeOmega (int [] syndromes,
	                          int [] lambda,
	                          int	deg_lambda,
	                          int [] omega) {
	   int deg_omega = 0;
	   for (int i = 0; i < nroots; i++){
	      int tmp = 0;
	      int j = (deg_lambda < i) ? deg_lambda : i;
	      for (; j >= 0; j--){
	         if ((poly2power (syndromes [i - j]) != codeLength) &&
	             (lambda [j] != codeLength)) {
	            int res = power2poly (
                                     multiply_power (
                                                   poly2power (
                                                         syndromes [i - j]),
                                                   lambda [j]));
	            tmp =  add_poly (tmp, res);
	         }
	      }

	      if (tmp != 0)
	         deg_omega = i;
	      omega [i] = poly2power (tmp);
	   }

	   omega [nroots] = codeLength;
	   return deg_omega;
	}
//
//
//
//
//	Basic encoder, returns - in bb - the parity bytes
	public void encode_rs (byte [] data, byte [] bb){
	int feedback;
	int [] workVector = new int [bb. length];
	Arrays. fill (workVector, 0);

	for (int i = 0; i < codeLength - nroots; i++){
	   feedback = poly2power (add_poly (data [i], workVector [0]));
	   if (feedback != codeLength){ /* feedback term is non-zero */
	      for (int j = 1; j < nroots; j++)
	         workVector [j] =
	                  add_poly (workVector [j],
	                      power2poly (
	                         multiply_power (feedback,
	                                   generator [nroots - j])));
	   }
/*	Shift */
	   for (int k = 0; k < nroots - 1; k ++)
	      workVector [k] = workVector [k + 1];
	   if (feedback != codeLength)
	      workVector [nroots - 1] =
	          power2poly (multiply_power (feedback,
	                                      generator [0]));
	   else
	      workVector [nroots - 1] = 0;
	}
	for (int i = 0; i < nroots; i ++)
	   bb [i] = (byte)(workVector [i] & 0xFF);
}

	private void enc (byte [] r, byte [] d, int cutlen) {
	byte [] rf  = new byte [codeLength];
	byte [] bb  = new byte [nroots];

	Arrays. fill (rf, (byte)0);
	for (int i = cutlen; i < codeLength; i++)
	   rf [i] = r[i - cutlen];

	encode_rs (rf, bb);
	for (int i = cutlen; i < codeLength - nroots; i++)
	   d [i - cutlen] = rf [i];
//	and the parity bytes
            System.arraycopy(bb, 0, d, codeLength - cutlen - nroots, nroots);
	}

}

