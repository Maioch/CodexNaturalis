package it.polimi.ingsw.view.gui.controllers;

import javafx.animation.*;
import javafx.scene.Node;

/**
 * Provides static methods used to perform animations on the GUI.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class Animator {

    //the interpolator used for all the animations done by this class
    private static final Interpolator cubicInterpolator = Interpolator.SPLINE(.02,.68,.33,.95);

    /**
     * Does a pop animation (vertical slide).
     *
     * @param node   the node to animate.
     * @param offset the y-offset of the node.
     * @param popUp  the boolean flagging whether the node has to pop up or dismiss.
     *
     * @return       the animation instance.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static TranslateTransition doPopAnimation(Node node, double offset, boolean popUp) {
        TranslateTransition animation = popUp ?
                prepareSlideAnimation(0,0, offset, 0):
                prepareSlideAnimation(0,0,0, offset);
        animation.setNode(node);
        animation.play();
        return animation;
    }

    /**
     * Does a slide animation (horizontal).
     *
     * @param node    the node to animate.
     * @param offset  the x-offset of the node.
     * @param slideIn the boolean flagging whether the node has to slide in or dismiss.
     *
     * @return        the animation instance.
     */
    public static TranslateTransition doSlideAnimation(Node node, double offset, boolean slideIn) {
        TranslateTransition animation = slideIn ?
                prepareSlideAnimation(offset,0,0, 0):
                prepareSlideAnimation(0,offset,0, 0);
        animation.setNode(node);
        animation.play();
        return animation;
    }

    /**
     * Does a fade animation.
     *
     * @param node   the node to animate.
     * @param fadeIn the boolean flagging whether the node has to fade in or dismiss.
     *
     * @return       the animation instance.
     */
    public static FadeTransition doFadeAnimation(Node node, boolean fadeIn) {
        FadeTransition animation = fadeIn ?
                prepareFadeAnimation(0,1):
                prepareFadeAnimation(1,0);
        animation.setNode(node);
        animation.play();
        return animation;
    }

    /**
     * Does a scale animation.
     *
     * @param node    the node to animate.
     * @param scaleIn the boolean flagging whether the node has to scale in or dismiss.
     *
     * @return        the animation instance.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static ScaleTransition doScaleAnimation(Node node, boolean scaleIn) {
        ScaleTransition animation = scaleIn ?
                prepareScaleAnimation(0,0,1,1):
                prepareScaleAnimation(1,1,0,0);
        animation.setNode(node);
        animation.play();
        return animation;
    }

    /**
     * Prepares a fade animation, without playing it.
     *
     * @param from the starting fade value.
     * @param to   the final fade value.
     *
     * @return     the prepared animation instance.
     */
    private static FadeTransition prepareFadeAnimation(double from, double to) {
        FadeTransition transition = new FadeTransition();
        transition.setFromValue(from);
        transition.setToValue(to);
        transition.setInterpolator(cubicInterpolator);
        transition.setRate(1.5);
        return transition;
    }

    /**
     * Prepares a slide animation, without playing it.
     *
     * @param startX the starting x-axis value.
     * @param endX   the final x-axis value.
     * @param startY the starting y-axis value.
     * @param endY   the final y-axis value.
     *
     * @return       the prepared animation instance.
     */
    private static TranslateTransition prepareSlideAnimation(double startX, double endX, double startY, double endY) {
        TranslateTransition transition = new TranslateTransition();
        transition.setFromX(startX);
        transition.setToX(endX);
        transition.setFromY(startY);
        transition.setToY(endY);
        transition.setInterpolator(cubicInterpolator);
        transition.setRate(1.5);
        return transition;
    }

    /**
     * Prepares a scale animation, without playing it.
     *
     * @param startX the starting x-axis value.
     * @param startY the starting y-axis value.
     * @param endX   the final x-axis value.
     * @param endY   the final y-axis value.
     *
     * @return       the prepared animation instance.
     */
    private static ScaleTransition prepareScaleAnimation(double startX, double startY, double endX, double endY) {
        ScaleTransition transition = new ScaleTransition();
        transition.setFromX(startX);
        transition.setToX(endX);
        transition.setFromY(startY);
        transition.setToY(endY);
        transition.setInterpolator(cubicInterpolator);
        transition.setRate(1.5);
        return transition;
    }
}