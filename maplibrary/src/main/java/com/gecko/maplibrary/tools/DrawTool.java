package com.gecko.maplibrary.tools;

import android.graphics.Color;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.util.ArrayList;

/**
 * Created by aiya on 2017/4/7.
 */

public class DrawTool {
    private ArrayList<Point> mPoints = new ArrayList<Point>();

    private ArrayList<Point> mMidPoints = new ArrayList<Point>();

    private SimpleMarkerSymbol mRedMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

    private SimpleMarkerSymbol mBlackMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

    private SimpleMarkerSymbol mGreenMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE);

    private GraphicsLayer mGraphicsLayerEditing;
    private MapView mMapView;

    /**
     * Draws polyline or polygon (dependent on current mEditMode) between the vertices in mPoints.
     */
    private void drawPolylineOrPolygon() {
        Graphic graphic;
        MultiPath multipath;

        // Create and add graphics layer if it doesn't already exist
        if (mGraphicsLayerEditing == null) {
            mGraphicsLayerEditing = new GraphicsLayer();
            mMapView.addLayer(mGraphicsLayerEditing);
        }

        if (mPoints.size() > 1) {

            // Build a MultiPath containing the vertices
            if (mEditMode == EditMode.POLYLINE) {
                multipath = new Polyline();
            } else {
                multipath = new Polygon();
            }
            multipath.startPath(mPoints.get(0));
            for (int i = 1; i < mPoints.size(); i++) {
                multipath.lineTo(mPoints.get(i));
            }

            // Draw it using a line or fill symbol
            if (mEditMode == EditMode.POLYLINE) {
                graphic = new Graphic(multipath, new SimpleLineSymbol(Color.BLACK, 4));
            } else {
                SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.YELLOW);
                simpleFillSymbol.setAlpha(100);
                simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 4));
                graphic = new Graphic(multipath, (simpleFillSymbol));
            }
            mGraphicsLayerEditing.addGraphic(graphic);
        }
    }

    /**
     * Draws mid-point half way between each pair of vertices in mPoints.
     */
    private void drawMidPoints() {
        int index;
        Graphic graphic;

        mMidPoints.clear();
        if (mPoints.size() > 1) {

            // Build new list of mid-points
            for (int i = 1; i < mPoints.size(); i++) {
                Point p1 = mPoints.get(i - 1);
                Point p2 = mPoints.get(i);
                mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
            }
            if (mEditMode == EditMode.POLYGON && mPoints.size() > 2) {
                // Complete the circle
                Point p1 = mPoints.get(0);
                Point p2 = mPoints.get(mPoints.size() - 1);
                mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
            }

            // Draw the mid-points
            index = 0;
            for (Point pt : mMidPoints) {
                if (mMidPointSelected && mInsertingIndex == index) {
                    graphic = new Graphic(pt, mRedMarkerSymbol);
                } else {
                    graphic = new Graphic(pt, mGreenMarkerSymbol);
                }
                mGraphicsLayerEditing.addGraphic(graphic);
                index++;
            }
        }
    }

    /**
     * Draws point for each vertex in mPoints.
     */
    private void drawVertices() {
        int index = 0;
        SimpleMarkerSymbol symbol;

        for (Point pt : mPoints) {
            if (mVertexSelected && index == mInsertingIndex) {
                // This vertex is currently selected so make it red
                symbol = mRedMarkerSymbol;
            } else if (index == mPoints.size() - 1 && !mMidPointSelected && !mVertexSelected) {
                // Last vertex and none currently selected so make it red
                symbol = mRedMarkerSymbol;
            } else {
                // Otherwise make it black
                symbol = mBlackMarkerSymbol;
            }
            Graphic graphic = new Graphic(pt, symbol);
            mGraphicsLayerEditing.addGraphic(graphic);
            index++;
        }
    }

    /**
     * Clears feature editing data and updates action bar.
     */
    void clear() {
        // Clear feature editing data
        mPoints.clear();
        mMidPoints.clear();
        mEditingStates.clear();

        mMidPointSelected = false;
        mVertexSelected = false;
        mInsertingIndex = 0;

        if (mGraphicsLayerEditing != null) {
            mGraphicsLayerEditing.removeAll();
        }

        // Update action bar to reflect the new state
        updateActionBar();
        int resId;
        switch (mEditMode) {
            case POINT:
                resId = R.string.title_add_point;
                break;
            case POLYGON:
                resId = R.string.title_add_polygon;
                break;
            case POLYLINE:
                resId = R.string.title_add_polyline;
                break;
            case NONE:
            default:
                resId = R.string.app_name;
                break;
        }
        getActionBar().setTitle(resId);
    }


}
