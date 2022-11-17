import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';

import { Category } from '../category';
import { CategoryService } from '../category.service';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {

  categoryList: Category[] = [];

  constructor(
    private location        : Location,
    private categoryService : CategoryService
  ) { }

  ngOnInit(): void {
    //this.getCategoryList();

    this.categoryList = [
      {id: 1, name: "test1", slug: "test", color: 0},
      {id: 2, name: "test2", slug: "test", color: 0},
      {id: 3, name: "test3", slug: "test", color: 0},
      {id: 4, name: "test4", slug: "test", color: 0},
      {id: 5, name: "test5", slug: "test", color: 0},
      {id: 6, name: "test6", slug: "test", color: 0},
      {id: 7, name: "test7", slug: "test", color: 0},
      {id: 8, name: "test8", slug: "test", color: 0},
    ]
  }

  getCategoryList(): void {
    this.categoryService.getCategoryList().subscribe(
      categoryList => this.categoryList = categoryList
    )
  }

  /*
  add(name: string): void {
    name = name.trim();
    if (!name) { return; }
    this.heroService.addHero({ name } as Hero)
      .subscribe(hero => {
        this.heroes.push(hero);
      });
  }
  */

  delete(category: Category): void {
    this.categoryList = this.categoryList.filter(t => t !== category);
    this.categoryService.deleteCategory(category.id).subscribe();
  }

  goBack(): void {
    this.location.back();
  }
}
