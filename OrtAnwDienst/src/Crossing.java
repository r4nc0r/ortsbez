/**
 * Created by aroldmi61242 on 08.06.2017.
 */
public class Crossing {

    int[] links;
    int[] neighbours;
    int timeTo;

    public Crossing(int id, nav.NavData navData)
    {
        links = navData.getLinksForCrossing(id);

        for (int i = 0; i < links.length; i++)
        {
            neighbours = new int[links.length];
            neighbours[i] = navData.getCrossingIDTo(links[i]);
        }
    }
}
