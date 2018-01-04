package net.dankito.deepthought.android.util.matchers;


import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.util.HumanReadables;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

/**
 * Thanks to dannyroa!
 * https://github.com/dannyroa/espresso-samples/blob/master/RecyclerView/app/src/androidTest/java/com/dannyroa/espresso_samples/recyclerview/RecyclerViewMatcher.java
 *
 * Created by dannyroa on 5/10/15.
 */
public class RecyclerViewMatcher {
    private final int recyclerViewId;

    public RecyclerViewMatcher(int recyclerViewId) {
        this.recyclerViewId = recyclerViewId;
    }

    public Matcher<View> atPosition(final int position) {
        return atPositionOnView(position, -1);
    }

    public Matcher<View> atPositionOnView(final int position, final int targetViewId) {

        return new ItemPositionInRecyclerViewMatcher(position, targetViewId);
    }


    public class ItemPositionInRecyclerViewMatcher extends TypeSafeMatcher<View> {
        protected Resources resources = null;
        protected View childView;

        protected int position;
        protected int targetViewId;

        public ItemPositionInRecyclerViewMatcher(int position, int targetViewId) {
            this.position = position;
            this.targetViewId = targetViewId;
        }


        public void describeTo(Description description) {
            String idDescription = Integer.toString(recyclerViewId);
            if (this.resources != null) {
                try {
                    idDescription = this.resources.getResourceName(recyclerViewId);
                } catch (Resources.NotFoundException var4) {
                    idDescription = String.format("%s (resource name not found)",
                        new Object[] { Integer.valueOf
                            (recyclerViewId) });
                }
            }

            description.appendText("with id: " + idDescription);
        }

        public boolean matchesSafely(View view) {

            this.resources = view.getResources();

            if (childView == null) {
                RecyclerView recyclerView = findRecyclerView(view);
                if (recyclerView != null && recyclerView.getId() == recyclerViewId) {
                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                    if(viewHolder != null) {
                        childView = viewHolder.itemView;
                    }
                }
                else {
                    return false;
                }
            }

            if (targetViewId == -1) {
                return view == childView;
            } else {
                View targetView = childView.findViewById(targetViewId);
                return view == targetView;
            }

        }

        protected RecyclerView findRecyclerView(View view) {
            return (RecyclerView) view.getRootView().findViewById(recyclerViewId);
        }
    }


    public static <VH extends RecyclerView.ViewHolder> ViewAction actionOnItemViewAtPosition(int position,
                                                                                             @IdRes
                                                                                                 int viewId,
                                                                                             ViewAction viewAction) {
        return new ActionOnItemViewAtPositionViewAction(position, viewId, viewAction);
    }

    private static final class ActionOnItemViewAtPositionViewAction<VH extends RecyclerView
        .ViewHolder>
        implements

        ViewAction {
        private final int position;
        private final ViewAction viewAction;
        private final int viewId;

        private ActionOnItemViewAtPositionViewAction(int position,
                                                     @IdRes int viewId,
                                                     ViewAction viewAction) {
            this.position = position;
            this.viewAction = viewAction;
            this.viewId = viewId;
        }

        public Matcher<View> getConstraints() {
            return Matchers.allOf(new Matcher[] {
                ViewMatchers.isAssignableFrom(RecyclerView.class), ViewMatchers.isDisplayed()
            });
        }

        public String getDescription() {
            return "actionOnItemAtPosition performing ViewAction: "
                + this.viewAction.getDescription()
                + " on item at position: "
                + this.position;
        }

        public void perform(UiController uiController, View view) {
            RecyclerView recyclerView = (RecyclerView) view;
            (new ScrollToPositionViewAction(this.position)).perform(uiController, view);
            uiController.loopMainThreadUntilIdle();

            View targetView = recyclerView.getChildAt(this.position).findViewById(this.viewId);

            if (targetView == null) {
                throw (new PerformException.Builder()).withActionDescription(this.toString())
                    .withViewDescription(

                        HumanReadables.describe(view))
                    .withCause(new IllegalStateException(
                        "No view with id "
                            + this.viewId
                            + " found at position: "
                            + this.position))
                    .build();
            } else {
                this.viewAction.perform(uiController, targetView);
            }
        }
    }

    private static final class ScrollToPositionViewAction implements ViewAction {
        private final int position;

        private ScrollToPositionViewAction(int position) {
            this.position = position;
        }

        public Matcher<View> getConstraints() {
            return Matchers.allOf(new Matcher[] {
                ViewMatchers.isAssignableFrom(RecyclerView.class), ViewMatchers.isDisplayed()
            });
        }

        public String getDescription() {
            return "scroll RecyclerView to position: " + this.position;
        }

        public void perform(UiController uiController, View view) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.scrollToPosition(this.position);
        }
    }


    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {

        return new RecyclerViewMatcher(recyclerViewId);
    }

}