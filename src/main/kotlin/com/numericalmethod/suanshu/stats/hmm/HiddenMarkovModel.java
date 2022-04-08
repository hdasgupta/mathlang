/*
 * Copyright (c) Numerical Method Inc.
 * http://www.numericalmethod.com/
 *
 * THIS SOFTWARE IS LICENSED, NOT SOLD.
 *
 * YOU MAY USE THIS SOFTWARE ONLY AS DESCRIBED IN THE LICENSE.
 * IF YOU ARE NOT AWARE OF AND/OR DO NOT AGREE TO THE TERMS OF THE LICENSE,
 * DO NOT USE THIS SOFTWARE.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITH NO WARRANTY WHATSOEVER,
 * EITHER EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION,
 * ANY WARRANTIES OF ACCURACY, ACCESSIBILITY, COMPLETENESS,
 * FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABILITY, NON-INFRINGEMENT,
 * TITLE AND USEFULNESS.
 *
 * IN NO EVENT AND UNDER NO LEGAL THEORY,
 * WHETHER IN ACTION, CONTRACT, NEGLIGENCE, TORT, OR OTHERWISE,
 * SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIMS, DAMAGES OR OTHER LIABILITIES,
 * ARISING AS A RESULT OF USING OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.numericalmethod.suanshu.stats.hmm;

import com.numericalmethod.suanshu.matrix.doubles.Matrix;

import static com.numericalmethod.suanshu.misc.SuanShuUtils.assertArgument;

import com.numericalmethod.suanshu.stats.markovchain.SimpleMC;
import com.numericalmethod.suanshu.stats.random.univariate.RandomNumberGenerator;
import com.numericalmethod.suanshu.vector.doubles.Vector;

import java.util.Arrays;

/**
 * In a (discrete) hidden Markov model, the state is not directly visible, but output, dependent on the state, is visible.
 * Each state has a probability distribution over the possible output tokens (could be continuous).
 * Therefore the sequence of tokens generated by an HMM gives some information about the sequence of states.
 * Note that the adjective 'hidden' refers to the state sequence through which the model passes,
 * not to the parameters of the model; even if the model parameters are known exactly, the model is still 'hidden'.
 * In other words, a hidden Markov model is a Markov chain of (hidden) states
 * and for each state a conditional random number generator (distribution).
 *
 * @author Haksun Li
 * @see <a href="http://en.wikipedia.org/wiki/Hidden_Markov_model">Wikipedia: Hidden Markov model</a>
 */
public class HiddenMarkovModel extends SimpleMC {

    /**
     * the conditional observation distributions (random number generators)
     */
    private final RandomNumberGenerator[] B;

    /**
     * Construct a hidden Markov model.
     *
     * @param PI the initial state probabilities
     * @param A  the state transition probabilities of the homogeneous hidden Markov chain
     * @param B  the conditional observation random number generators (distributions)
     */
    public HiddenMarkovModel(Vector PI, Matrix A, RandomNumberGenerator[] B) {
        super(PI, A);

        assertArgument(B.length == A.nRows(), "the number of rows in B is the same as the number of states");
        this.B = Arrays.copyOf(B, A.nRows());
    }

    /**
     * Construct a hidden Markov model using the stationary probabilities of the initial states.
     *
     * @param A the state transition probabilities of the homogeneous hidden Markov chain
     * @param B the conditional observation random number generators (distributions)
     */
    public HiddenMarkovModel(Matrix A, RandomNumberGenerator[] B) {
        this(getStationaryProbabilities(A), A, B);
    }

    /**
     * Copy constructor.
     *
     * @param that a {@code HiddenMarkovModel}
     */
    public HiddenMarkovModel(HiddenMarkovModel that) {
        this(that.PI(), that.A(), that.B);
    }

    @Override
    public void seed(long... seeds) {
        for (int i = 0; i < B.length; ++i) {
            B[i].seed(seeds);
        }

        super.seed(seeds);
    }

    /**
     * Get the next simulated observation.
     *
     * @return next observation
     */
    @Override
    public double nextDouble() {
        return B[nextState() - 1].nextDouble();//state counts from 1
    }

    /**
     * Get the next simulated innovation - state and observation.
     *
     * @return the next HMM innovation
     */
    public HmmInnovation next() {
        int qt = nextState();//simulate state
        double ot = B[qt - 1].nextDouble();//simuate observation
        return new HmmInnovation(qt, ot);
    }
}