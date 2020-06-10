package ir.ngra.drivertrafficcontroller.models;

public class ModelAdabterSuggestion {

    private String address;

    private boolean LoadMore;

    public ModelAdabterSuggestion(String address, boolean loadMore) {
        this.address = address;
        LoadMore = loadMore;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isLoadMore() {
        return LoadMore;
    }

    public void setLoadMore(boolean loadMore) {
        LoadMore = loadMore;
    }
}
