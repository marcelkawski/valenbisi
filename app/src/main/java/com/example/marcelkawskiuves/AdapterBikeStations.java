package com.example.marcelkawskiuves;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.concurrent.ExecutionException;



public class AdapterBikeStations extends android.widget.BaseAdapter {

    private ArrayList<BikeStation> bikeStations;
    private DBHelper dbHelper;
    private DataCollector dataCollector;
    private Location deviceLocation;
    private String distanceString;
    private String distanceUnit;
    Context context;

    static class ViewHolder {
        TextView number;
        TextView name;
        TextView distance;
        //TextView distanceUnit;
        TextView reports;
        TextView available;
    }

    public AdapterBikeStations(Context c, Location location) {

        this.context = c;
        this.dbHelper = new DBHelper(c);
        this.dataCollector = new DataCollector();
        this.deviceLocation = location;
        try {
            this.bikeStations = dataCollector.execute().get();
            for (BikeStation bikeStation: bikeStations) {
                bikeStation.calculateDistance(deviceLocation);
            }
            Collections.sort(bikeStations, new StationsComparator());
            System.out.println("PO SORTOWANIUUUUUUUUUUU");
            System.out.println(location);
            for (BikeStation bikeStation: bikeStations) {
                System.out.println(bikeStation.getNumber());
            }
            Collections.sort(bikeStations, new StationsComparator());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return bikeStations.size();
    }

    @Override
    public BikeStation getItem(int index){
        System.out.println("INDEEEEEEEEEEX");
        System.out.println(index);
        return bikeStations.get(index);
    }

    @Override
    public long getItemId(int position) {
        return bikeStations.get(position).getNumber();
    }

    public String getDistanceString() {
        return distanceString;
    }

    public String getDistanceUnit() {
        return distanceUnit;
    }

    public ArrayList<BikeStation> getBikeStations() {
        return bikeStations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            LayoutInflater li =
                    (LayoutInflater)context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.bikestationview, null) ;
            holder = new ViewHolder();

            holder.number = v.findViewById(R.id.bikestationviewnumber);
            holder.name = v.findViewById(R.id.bikestationviewname);
            holder.distance = v.findViewById(R.id.bikestationviewdistance);
            //holder.distanceUnit = v.findViewById(R.id.bikestationviewdistanceunit);
            holder.reports = v.findViewById(R.id.bikestationviewreports);
            holder.available = v.findViewById(R.id.bikestationviewavailable);
            v.setTag(holder);
        } else {
            holder = (ViewHolder)v.getTag();
        }

        if (bikeStations.get(position).getAvailable() > 10) {
            v.setBackgroundColor(Color.GREEN);
        }
        else if (bikeStations.get(position).getAvailable() > 4)
            v.setBackgroundColor(Color.YELLOW);
        else v.setBackgroundColor(Color.RED);

        BikeStation bikeStation = bikeStations.get(position);
        String name = bikeStation.getName();
        name = name.substring(name.indexOf("_")+1).replaceAll("_", " ");

        double distance = bikeStation.getDistance();
        if (distance >= 1000) {
            distanceUnit = "km";
            //holder.distanceUnit.setText(distanceUnit);
            distance /= 1000;
            distanceString = String.format("%.1f", distance) + distanceUnit;
        }
        else {
            distanceUnit = "m";
            //holder.distanceUnit.setText(distanceUnit);
            distanceString = String.format("%.0f", distance) + distanceUnit;
        }

        holder.number.setText(String.valueOf(bikeStation.getNumber()));
        holder.name.setText(name);
        holder.distance.setText(distanceString);
        holder.reports.setText(String.valueOf(dbHelper.countReports(bikeStation.getNumber())));
        holder.available.setText(String.valueOf(bikeStation.getAvailable()));

        return v;
    }


    public class StationsComparator implements Comparator<BikeStation> {

        @Override
        public int compare(BikeStation bs1, BikeStation bs2) {
            double distance1 = bs1.getDistance();
            double distance2 = bs2.getDistance();
            if (distance1 > distance2) {
                return 1;
            } else if (distance1 < distance2) {
                return -1;
            }
            return 0;
        }
    }

}
