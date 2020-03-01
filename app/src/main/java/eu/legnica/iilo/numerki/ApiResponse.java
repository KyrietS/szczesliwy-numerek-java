package eu.legnica.iilo.numerki;

import java.util.List;

class ApiResponse {
    List<Day> days;

    class Day{
        String date;
        List<Integer> numbers;
    }
}
