package src.toi_et_moi.mgdp.init;

public interface IFlipData {
	int mgdp$getFlipProgress();
	void mgdp$setFlipProgress(int progress);
	float mgdp$getWindmill();
	void mgdp$setWindmill(float angle);
	int mgdp$getSbShields();
	void mgdp$setSbShields(int shields);
	int mgdp$getSbHp();
	void mgdp$setSbHp(int hp);
	int mgdp$getCsShields();
	void mgdp$setCsShields(int shields);
}
