/**
 * @jest-environment jsdom
 */
import BreadcrumbWithCircle from "@/components/toolbars/BreadcrumbWithCircle.vue";
import usePopups, {
  PopupInfo,
} from "../../src/components/commons/Popups/usePopup";
import helper from "../helpers";

helper.resetWithApiMock(beforeEach, afterEach);

describe("breadcrumb with circles", () => {
  const popupInfo = [] as PopupInfo[];
  beforeEach(() => {
    usePopups().popups.register({ popupInfo });
  });
  it("render the breadcrumber", async () => {
    const wrapper = helper
      .component(BreadcrumbWithCircle)
      .withProps({})
      .mount();
    expect(wrapper.find(".breadcrumb-item").text()).toEqual("My Notes");
  });

  it("opens the circles selection", async () => {
    const wrapper = helper
      .component(BreadcrumbWithCircle)
      .withProps({})
      .mount();
    wrapper.find("[role='button']").trigger("click");
    expect(popupInfo).toHaveLength(1);
  });
});