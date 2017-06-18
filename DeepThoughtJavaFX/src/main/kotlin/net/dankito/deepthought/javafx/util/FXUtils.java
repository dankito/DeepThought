package net.dankito.deepthought.javafx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javafx.application.Platform;
import javafx.css.Styleable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Window;


public class FXUtils {

  public final static String HtmlEditorDefaultText = "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>";

  public static final Double SizeMaxValue = Double.MAX_VALUE;


  private final static Logger log = LoggerFactory.getLogger(FXUtils.class);


  public static void runOnUiThread(Runnable runnable) {
    if(Platform.isFxApplicationThread()) {
      runnable.run();
    }
    else {
      Platform.runLater(runnable);
    }
  }

  public static void ensureNodeOnlyUsesSpaceIfVisible(Node node) {
    node.managedProperty().bind(node.visibleProperty());
  }

  public static void setBackgroundToColor(Region region, Color color) {
    region.setBackground(new Background(new BackgroundFill(color, new CornerRadii(0), new Insets(0))));
  }


  // thanks for this code to https://stackoverflow.com/questions/12837592/how-to-scroll-to-make-a-node-within-the-content-of-a-scrollpane-visible
  public static void scrollToNode(ScrollPane pane, Node node) {
    double width = pane.getContent().getBoundsInLocal().getWidth();
    double height = pane.getContent().getBoundsInLocal().getHeight();

    double x = node.getBoundsInParent().getMaxX();
    double y = node.getBoundsInParent().getMaxY();

    // scrolling values range from 0 to 1
    pane.setVvalue(y/height);
    pane.setHvalue(x/width);

    // just for usability
    node.requestFocus();
  }


  public static void showSplitPaneDividers(SplitPane splitPane, boolean show) {
    splitPane.lookupAll(".split-pane-divider").stream()
        .forEach(div -> div.setMouseTransparent(!show));
  }

  public static Point2D getNodeScreenCoordinates(Node node) {
    // thanks for pointing me in the right direction how to calculate a Node's Screen position to: http://blog.crisp.se/2012/08/29/perlundholm/window-scene-and-node-coordinates-in-javafx
    Scene scene = node.getScene();
    Window window = scene.getWindow();

    Point2D windowCoord = new Point2D(window.getX(), window.getY());
    Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
    Point2D nodeCoord = node.localToScene(0, 0);

    return new Point2D(Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX()),
                       Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY()));
  }

  public static Screen getScreenWindowLeftUpperCornerIsIn(Window window) {
    List<Screen> screens = Screen.getScreensForRectangle(window.getX(), window.getY(), 1, 1);
    if(screens.size() > 0)
      return screens.get(0);

    return null;
  }


  protected static boolean isSelectAllEvent(KeyEvent event) {
    return event.isShortcutDown() && event.getCode() == KeyCode.A;
  }

  protected static boolean isPasteEvent(KeyEvent event) {
    return event.isShortcutDown() && event.getCode() == KeyCode.V;
  }

  protected static boolean isCharacterKeyReleased(KeyEvent event) {
    // Make custom changes here..
    switch (event.getCode())
    {
      case ALT:
      case COMMAND:
      case CONTROL:
      case SHIFT:
        return false;
      default:
        return true;
    }
  }


  public static void addStyleToCurrentStyle(Node node, String styleToAdd) {
    String style = addStyleToCurrentStyleString(node, styleToAdd);
    node.setStyle(style);
  }

  public static void addStyleToCurrentStyle(MenuItem item, String styleToAdd) {
    String style = addStyleToCurrentStyleString(item, styleToAdd);
    item.setStyle(style);
  }

  protected static String addStyleToCurrentStyleString(Styleable styleable, String styleToAdd) {
    String style = styleable.getStyle();
    style = style == null ? styleToAdd : style + " " + styleToAdd;
    return style;
  }

  public static boolean isNoModifierPressed(KeyEvent event) {
    return event.isControlDown() == false && event.isShiftDown() == false && event.isAltDown() == false && event.isMetaDown() == false && event.isShortcutDown() == false;
  }

}
