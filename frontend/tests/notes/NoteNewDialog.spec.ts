import { flushPromises, VueWrapper } from "@vue/test-utils";
import { ComponentPublicInstance } from "vue";
import NoteNewDialog from "@/components/notes/NoteNewDialog.vue";
import helper from "../helpers";
import makeMe from "../fixtures/makeMe";

helper.resetWithApiMock(beforeEach, afterEach);

describe("adding new note", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.runOnlyPendingTimers();
    vi.useRealTimers();
  });

  const note = makeMe.aNote.topic("mythical").please();

  it("search for duplicate", async () => {
    helper.apiMock.expectingPost(`/api/notes/search`).andReturnOnce([note]);
    const wrapper = helper
      .component(NoteNewDialog)
      .withStorageProps({ parentId: 123 })
      .mount();
    await wrapper.find("input#note-topic").setValue("myth");

    vi.runOnlyPendingTimers();
    await flushPromises();

    expect(wrapper.text()).toContain("mythical");
  });

  describe("submit form", () => {
    let wrapper: VueWrapper<ComponentPublicInstance>;

    beforeEach(async () => {
      wrapper = helper
        .component(NoteNewDialog)
        .withStorageProps({ parentId: 123 })
        .mount({ attachTo: document.body });
      await wrapper.find("input#note-topic").setValue("note title");
      vi.clearAllTimers();
    });

    it("call the api", async () => {
      helper.apiMock.expectingPost(`/api/notes/123/create`);
      await wrapper.find("form").trigger("submit");
    });

    it("call the api once only", async () => {
      helper.apiMock.expectingPost(`/api/notes/123/create`);
      await wrapper.find("form").trigger("submit");
      await wrapper.find("form").trigger("submit");
    });
  });

  describe("search wikidata entry", () => {
    let wrapper: VueWrapper<ComponentPublicInstance>;

    beforeEach(() => {
      helper.apiMock.expectingPost(`/api/notes/search`).andReturnOnce([]);
      wrapper = helper
        .component(NoteNewDialog)
        .withStorageProps({ parentId: 123 })
        .mount({ attachTo: document.body });
    });

    const titleInput = () => {
      return wrapper.find("input#note-topic");
    };

    const searchWikidata = async (key: string) => {
      await titleInput().setValue(key);
      await wrapper.find("button[title='Wikidata Id']").trigger("click");

      await flushPromises();

      return wrapper.find('select[name="wikidataSearchResult"]');
    };

    const searchAndSelectFirstResult = async (key: string) => {
      const select = await searchWikidata(key);
      select.findAll("option").at(1)?.setValue();
    };

    const replaceTitle = async () => {
      await wrapper.find("[id='topicRadio-Replace']").setValue();
    };
    const appendTitle = async () => {
      await wrapper.find("[id='topicRadio-Append']").setValue();
    };
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    const doNothing = () => {};

    describe("the select for wikidata id", () => {
      let select;
      beforeEach(async () => {
        const searchResult = makeMe.aWikidataSearchEntity.label("dog").please();
        helper.apiMock
          .expectingGet(`/api/wikidata/search/dog`)
          .andReturnOnce([searchResult]);
        select = await searchWikidata("dog");
      });

      it("focus on the select", async () => {
        expect(select.element).toHaveFocus();
      });

      it("remove the select when lose focus", async () => {
        select.element.blur();
        await flushPromises();
        expect(wrapper.vm.$el).not.toContainElement(select.element);
      });
    });

    it.each`
      searchTitle | wikidataTitle | action          | expectedTitle
      ${"dog"}    | ${"dog"}      | ${doNothing}    | ${"dog"}
      ${"dog"}    | ${"Dog"}      | ${doNothing}    | ${"Dog"}
      ${"dog"}    | ${"Canine"}   | ${replaceTitle} | ${"Canine"}
      ${"dog"}    | ${"Canine"}   | ${appendTitle}  | ${"dog / Canine"}
    `(
      "search $searchTitle get $wikidataTitle and choose to $action",
      async ({ searchTitle, wikidataTitle, action, expectedTitle }) => {
        const searchResult = makeMe.aWikidataSearchEntity
          .label(wikidataTitle)
          .please();

        helper.apiMock
          .expectingGet(`/api/wikidata/search/${searchTitle}`)
          .andReturnOnce([searchResult]);
        await searchAndSelectFirstResult(searchTitle);

        action();
        await flushPromises();

        expect((<HTMLInputElement>titleInput().element).value).toBe(
          expectedTitle,
        );
      },
    );
  });
});
